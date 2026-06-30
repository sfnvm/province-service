package com.sfnvm.province.web.dto

/** A type classifier (Municipality, Province, Ward, Commune, Special region). */
data class AdministrativeUnitDto(
  val id: Int,
  val fullName: String?,
  val fullNameEn: String?,
  val shortName: String?,
  val shortNameEn: String?,
  val codeName: String?,
  val codeNameEn: String?,
)

/** A geographic region (standalone reference data, not linked to provinces). */
data class AdministrativeRegionDto(
  val id: Int,
  val name: String,
  val nameEn: String,
  val codeName: String?,
  val codeNameEn: String?,
)

data class ProvinceDto(
  val code: String,
  val name: String,
  val nameEn: String?,
  val fullName: String,
  val fullNameEn: String?,
  val codeName: String?,
  val administrativeUnit: AdministrativeUnitDto?,
)

data class WardDto(
  val code: String,
  val name: String,
  val nameEn: String?,
  val fullName: String?,
  val fullNameEn: String?,
  val codeName: String?,
  val provinceCode: String?,
  val administrativeUnit: AdministrativeUnitDto?,
)
