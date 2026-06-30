package com.sfnvm.province.service

import com.sfnvm.province.repository.AdministrativeRegionRepository
import com.sfnvm.province.repository.AdministrativeUnitRepository
import com.sfnvm.province.web.dto.AdministrativeRegionDto
import com.sfnvm.province.web.dto.AdministrativeUnitDto
import com.sfnvm.province.web.error.ResourceNotFoundException
import com.sfnvm.province.web.mapper.toDto
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ReferenceService(
  private val units: AdministrativeUnitRepository,
  private val regions: AdministrativeRegionRepository,
) {

  fun listUnits(): List<AdministrativeUnitDto> = units.findAll(Sort.by("id")).map { it.toDto() }

  fun getUnit(id: Int): AdministrativeUnitDto =
    units
      .findById(id)
      .map { it.toDto() }
      .orElseThrow {
        ResourceNotFoundException("administrative-unit", id)
      }

  fun listRegions(): List<AdministrativeRegionDto> =
    regions.findAll(Sort.by("id")).map { it.toDto() }

  fun getRegion(id: Int): AdministrativeRegionDto =
    regions
      .findById(id)
      .map { it.toDto() }
      .orElseThrow {
        ResourceNotFoundException("administrative-region", id)
      }
}
