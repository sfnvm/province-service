package com.sfnvm.province.repository

import com.sfnvm.province.domain.AdministrativeRegion
import com.sfnvm.province.domain.AdministrativeUnit
import com.sfnvm.province.domain.Province
import com.sfnvm.province.domain.Ward
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AdministrativeRegionRepository : JpaRepository<AdministrativeRegion, Int>

interface AdministrativeUnitRepository : JpaRepository<AdministrativeUnit, Int>

interface ProvinceRepository : JpaRepository<Province, String> {

  /** [pattern] is a pre-lowercased `%term%` (or null for no text filter). */
  @EntityGraph(attributePaths = ["administrativeUnit"])
  @Query(
    """
    SELECT p FROM Province p
    WHERE (:pattern IS NULL
           OR lower(p.name) LIKE :pattern
           OR lower(p.fullName) LIKE :pattern
           OR lower(p.codeName) LIKE :pattern)
      AND (:unitId IS NULL OR p.administrativeUnit.id = :unitId)
    """
  )
  fun search(
    @Param("pattern") pattern: String?,
    @Param("unitId") unitId: Int?,
    pageable: Pageable,
  ): Page<Province>

  @EntityGraph(attributePaths = ["administrativeUnit"]) fun findByCode(code: String): Province?
}

interface WardRepository : JpaRepository<Ward, String> {

  /** [pattern] is a pre-lowercased `%term%` (or null for no text filter). */
  @EntityGraph(attributePaths = ["administrativeUnit"])
  @Query(
    """
    SELECT w FROM Ward w
    WHERE (:pattern IS NULL
           OR lower(w.name) LIKE :pattern
           OR lower(w.fullName) LIKE :pattern
           OR lower(w.codeName) LIKE :pattern)
      AND (:provinceCode IS NULL OR w.provinceCode = :provinceCode)
      AND (:unitId IS NULL OR w.administrativeUnit.id = :unitId)
    """
  )
  fun search(
    @Param("pattern") pattern: String?,
    @Param("provinceCode") provinceCode: String?,
    @Param("unitId") unitId: Int?,
    pageable: Pageable,
  ): Page<Ward>

  @EntityGraph(attributePaths = ["administrativeUnit"]) fun findByCode(code: String): Ward?
}
