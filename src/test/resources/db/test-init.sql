-- Minimal fixture mirroring the real schema + a few rows (incl. Unicode names)
-- for repository/integration tests. Loaded once via Testcontainers initScript.

CREATE TABLE administrative_regions (
  id           int PRIMARY KEY,
  name         varchar(255) NOT NULL,
  name_en      varchar(255) NOT NULL,
  code_name    varchar(255),
  code_name_en varchar(255)
);

CREATE TABLE administrative_units (
  id            int PRIMARY KEY,
  full_name     varchar(255),
  full_name_en  varchar(255),
  short_name    varchar(255),
  short_name_en varchar(255),
  code_name     varchar(255),
  code_name_en  varchar(255)
);

CREATE TABLE provinces (
  code                   varchar(20) PRIMARY KEY,
  name                   varchar(255) NOT NULL,
  name_en                varchar(255),
  full_name              varchar(255) NOT NULL,
  full_name_en           varchar(255),
  code_name              varchar(255),
  administrative_unit_id int REFERENCES administrative_units (id)
);

CREATE TABLE wards (
  code                   varchar(20) PRIMARY KEY,
  name                   varchar(255) NOT NULL,
  name_en                varchar(255),
  full_name              varchar(255),
  full_name_en           varchar(255),
  code_name              varchar(255),
  province_code          varchar(20) REFERENCES provinces (code),
  administrative_unit_id int REFERENCES administrative_units (id)
);

INSERT INTO administrative_units (id, full_name, full_name_en, short_name, short_name_en, code_name, code_name_en) VALUES
  (1, 'Thành phố trực thuộc trung ương', 'Municipality', 'Thành phố', 'City', 'thanh_pho_truc_thuoc_trung_uong', 'municipality'),
  (2, 'Tỉnh', 'Province', 'Tỉnh', 'Province', 'tinh', 'province'),
  (3, 'Phường', 'Ward', 'Phường', 'Ward', 'phuong', 'ward');

INSERT INTO administrative_regions (id, name, name_en, code_name, code_name_en) VALUES
  (3, 'Đồng bằng sông Hồng', 'Red River Delta', 'dong_bang_song_hong', 'red_river_delta');

INSERT INTO provinces (code, name, name_en, full_name, full_name_en, code_name, administrative_unit_id) VALUES
  ('01', 'Hà Nội', 'Ha Noi', 'Thành phố Hà Nội', 'Ha Noi City', 'ha_noi', 1),
  ('04', 'Cao Bằng', 'Cao Bang', 'Tỉnh Cao Bằng', 'Cao Bang Province', 'cao_bang', 2);

INSERT INTO wards (code, name, name_en, full_name, full_name_en, code_name, province_code, administrative_unit_id) VALUES
  ('00004', 'Ba Đình', 'Ba Dinh', 'Phường Ba Đình', 'Ba Dinh Ward', 'ba_dinh', '01', 3),
  ('00008', 'Ngọc Hà', 'Ngoc Ha', 'Phường Ngọc Hà', 'Ngoc Ha Ward', 'ngoc_ha', '01', 3);
