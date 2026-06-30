package com.sfnvm.province.service

import com.sfnvm.province.repository.ProvinceRepository
import com.sfnvm.province.repository.WardRepository
import com.sfnvm.province.web.dto.ProvinceDto
import com.sfnvm.province.web.dto.WardDto
import com.sfnvm.province.web.error.ResourceNotFoundException
import com.sfnvm.province.web.mapper.toDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProvinceService(
  private val provinces: ProvinceRepository,
  private val wards: WardRepository,
) {

  fun list(q: String?, unitId: Int?, pageable: Pageable): Page<ProvinceDto> =
    provinces.search(likePattern(q), unitId, pageable).map { it.toDto() }

  fun get(code: String): ProvinceDto =
    provinces.findByCode(code)?.toDto() ?: throw ResourceNotFoundException("province", code)

  fun listWards(code: String, q: String?, pageable: Pageable): Page<WardDto> {
    if (!provinces.existsById(code)) throw ResourceNotFoundException("province", code)
    return wards.search(likePattern(q), code, null, pageable).map { it.toDto() }
  }
}

/** Treat blank/whitespace-only search terms as "no filter". */
internal fun String?.normalize(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

/** Build a lowercased `%term%` LIKE pattern, or null when there is no search term. */
internal fun likePattern(q: String?): String? = q.normalize()?.lowercase()?.let { "%$it%" }
