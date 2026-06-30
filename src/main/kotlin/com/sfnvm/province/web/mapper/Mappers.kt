package com.sfnvm.province.web.mapper

import com.sfnvm.province.domain.AdministrativeRegion
import com.sfnvm.province.domain.AdministrativeUnit
import com.sfnvm.province.domain.Province
import com.sfnvm.province.domain.Ward
import com.sfnvm.province.web.dto.AdministrativeRegionDto
import com.sfnvm.province.web.dto.AdministrativeUnitDto
import com.sfnvm.province.web.dto.ProvinceDto
import com.sfnvm.province.web.dto.WardDto

fun AdministrativeUnit.toDto() =
  AdministrativeUnitDto(
    id = id,
    fullName = fullName,
    fullNameEn = fullNameEn,
    shortName = shortName,
    shortNameEn = shortNameEn,
    codeName = codeName,
    codeNameEn = codeNameEn,
  )

fun AdministrativeRegion.toDto() =
  AdministrativeRegionDto(
    id = id,
    name = name,
    nameEn = nameEn,
    codeName = codeName,
    codeNameEn = codeNameEn,
  )

fun Province.toDto() =
  ProvinceDto(
    code = code,
    name = name,
    nameEn = nameEn,
    fullName = fullName,
    fullNameEn = fullNameEn,
    codeName = codeName,
    administrativeUnit = administrativeUnit?.toDto(),
  )

fun Ward.toDto() =
  WardDto(
    code = code,
    name = name,
    nameEn = nameEn,
    fullName = fullName,
    fullNameEn = fullNameEn,
    codeName = codeName,
    provinceCode = provinceCode,
    administrativeUnit = administrativeUnit?.toDto(),
  )
