package com.sfnvm.province.service

import com.sfnvm.province.repository.WardRepository
import com.sfnvm.province.web.dto.WardDto
import com.sfnvm.province.web.error.ResourceNotFoundException
import com.sfnvm.province.web.mapper.toDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WardService(private val wards: WardRepository) {

  fun list(q: String?, provinceCode: String?, unitId: Int?, pageable: Pageable): Page<WardDto> =
    wards.search(likePattern(q), provinceCode.normalize(), unitId, pageable).map { it.toDto() }

  fun get(code: String): WardDto =
    wards.findByCode(code)?.toDto() ?: throw ResourceNotFoundException("ward", code)
}
