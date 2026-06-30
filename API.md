# province-service API

Read-only REST API over Vietnam's **post-2025 2-tier** administrative dataset
(34 provinces → 3,321 wards; districts abolished). All responses are JSON (UTF-8).
Base path: **`/api/v1`**. Errors are RFC-7807 `ProblemDetail`.

## Conventions

- **Pagination** (paged endpoints): `page` (0-based), `size` (default 20, **max 200**),
  `sort` (e.g. `sort=code,asc`). Paged responses are `{ "content": [...], "page": { size, number, totalElements, totalPages } }`.
- **Search `q`**: case-insensitive substring match over `name`, `full_name`, **and** the
  ASCII-folded `code_name` — so `Hà Nội`, `ha noi`, and `ha_noi` all match (Unicode-aware).
- **Codes** are strings (`provinces.code` e.g. `01`, `wards.code` e.g. `00004`).

## Endpoints

| Method | Path | Query params | Returns |
|---|---|---|---|
| GET | `/api/v1/provinces` | `q`, `unitId`, `page`, `size`, `sort` | Page of `ProvinceDto` |
| GET | `/api/v1/provinces/{code}` | — | `ProvinceDto` · 404 if absent |
| GET | `/api/v1/provinces/{code}/wards` | `q`, `page`, `size`, `sort` | Page of `WardDto` · 404 if province absent |
| GET | `/api/v1/wards` | `q`, `provinceCode`, `unitId`, `page`, `size`, `sort` | Page of `WardDto` |
| GET | `/api/v1/wards/{code}` | — | `WardDto` · 404 if absent |
| GET | `/api/v1/administrative-units` | — | `List<AdministrativeUnitDto>` (5) |
| GET | `/api/v1/administrative-units/{id}` | — | `AdministrativeUnitDto` · 404 if absent |
| GET | `/api/v1/administrative-regions` | — | `List<AdministrativeRegionDto>` (8) |
| GET | `/api/v1/administrative-regions/{id}` | — | `AdministrativeRegionDto` · 404 if absent |

`administrative_units` is a type classifier (Municipality/Province/Ward/Commune/Special) —
filter provinces/wards by it with `?unitId=`. `administrative_regions` is standalone
reference data (not linked to provinces), so it has no nested children.

## Response shapes

`ProvinceDto`
```json
{
  "code": "01",
  "name": "Hà Nội",
  "nameEn": "Ha Noi",
  "fullName": "Thành phố Hà Nội",
  "fullNameEn": "Ha Noi City",
  "codeName": "ha_noi",
  "administrativeUnit": {
    "id": 1, "fullName": "Thành phố trực thuộc trung ương", "fullNameEn": "Municipality",
    "shortName": "Thành phố", "shortNameEn": "City",
    "codeName": "thanh_pho_truc_thuoc_trung_uong", "codeNameEn": "municipality"
  }
}
```

`WardDto`
```json
{
  "code": "00004",
  "name": "Ba Đình",
  "nameEn": "Ba Dinh",
  "fullName": "Phường Ba Đình",
  "fullNameEn": null,
  "codeName": "ba_dinh",
  "provinceCode": "01",
  "administrativeUnit": { "id": 3, "shortName": "Phường", "...": "..." }
}
```

`AdministrativeRegionDto`: `{ id, name, nameEn, codeName, codeNameEn }`

## Errors (RFC-7807)
```json
{ "title": "Resource not found", "status": 404,
  "detail": "province '99' not found", "instance": "/api/v1/provinces/99" }
```

## Examples
```bash
curl 'localhost:8080/api/v1/provinces?q=Hà&size=5&sort=code'
curl 'localhost:8080/api/v1/provinces/01'
curl 'localhost:8080/api/v1/provinces/01/wards?q=ngoc&size=10'
curl 'localhost:8080/api/v1/wards?provinceCode=01&unitId=3'
curl 'localhost:8080/api/v1/administrative-units'
```

## Running locally
- `local` profile (throwaway Postgres via compose): `./scripts/run-local.sh`
- `dev` profile (real DB over WireGuard): `./scripts/run-dev.sh`
