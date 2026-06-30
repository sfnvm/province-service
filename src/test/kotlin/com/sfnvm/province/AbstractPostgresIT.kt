package com.sfnvm.province

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

/**
 * Shared Postgres container (loaded with the test fixture) wired into Spring via
 *
 * @ServiceConnection. Subclasses get a real Postgres with the four tables + sample rows; the `test`
 *   profile disables Flyway and Hibernate DDL.
 */
@Testcontainers
@ActiveProfiles("test")
abstract class AbstractPostgresIT {
  companion object {
    @Container
    @ServiceConnection
    @JvmStatic
    val postgres = PostgreSQLContainer("postgres:17").withInitScript("db/test-init.sql")
  }
}
