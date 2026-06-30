-- Grant the CloudNativePG `app` role full ownership of the vietnamese_provinces
-- database, so the service (and Flyway) can fully manage its own schema.
--
-- The CNPG superuser bootstraps the database, but leaves `app` as a connect-only
-- user without CREATE on `public` — which breaks Flyway ("non-empty schema, no
-- history table" / "permission denied for schema public"). This fixes that.
--
-- Run as a SUPERUSER, connected TO the vietnamese_provinces database, e.g.:
--   set -a; source ~/sauces/k8s/configs/postgres-cnpg/local-secret-postgres-superuser.env; set +a
--   PGPASSWORD="$POSTGRES_SUPERUSER_PASSWORD" psql \
--     "host=$POSTGRES_SUPERUSER_VPN_PRIMARY_HOST port=$POSTGRES_SUPERUSER_VPN_PORT \
--      dbname=vietnamese_provinces user=$POSTGRES_SUPERUSER_USER \
--      sslmode=$POSTGRES_SUPERUSER_VPN_SSLMODE" -v ON_ERROR_STOP=1 -f scripts/db-grants.sql
--
-- Idempotent: safe to re-run. On a fresh DB the loops are no-ops (Flyway-created
-- tables are owned by `app` anyway); on the existing DB they transfer the
-- hand-loaded tables. For the k8s deploy, run this as a CNPG bootstrap/post-init.

ALTER DATABASE vietnamese_provinces OWNER TO app;
GRANT ALL PRIVILEGES ON DATABASE vietnamese_provinces TO app;
GRANT ALL ON SCHEMA public TO app;

-- Hand ownership of every existing object in `public` to `app` (so it can
-- ALTER/DROP them in future migrations, not just read/write).
DO $$
DECLARE r record;
BEGIN
  FOR r IN SELECT format('%I.%I', schemaname, tablename) AS obj
           FROM pg_tables WHERE schemaname = 'public'
  LOOP EXECUTE format('ALTER TABLE %s OWNER TO app', r.obj); END LOOP;

  FOR r IN SELECT format('%I.%I', schemaname, sequencename) AS obj
           FROM pg_sequences WHERE schemaname = 'public'
  LOOP EXECUTE format('ALTER SEQUENCE %s OWNER TO app', r.obj); END LOOP;
END $$;

GRANT ALL ON ALL TABLES    IN SCHEMA public TO app;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO app;

-- Future objects created in `public` are granted to `app` automatically.
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES    TO app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO app;
