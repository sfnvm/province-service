package com.sfnvm.province.web

import com.sfnvm.province.service.ProvinceService
import com.sfnvm.province.web.dto.ProvinceDto
import com.sfnvm.province.web.dto.WardDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/provinces")
class ProvinceController(private val service: ProvinceService) {

  @GetMapping
  fun list(
    @RequestParam(required = false) q: String?,
    @RequestParam(required = false) unitId: Int?,
    @PageableDefault(size = 20, sort = ["code"]) pageable: Pageable,
  ): Page<ProvinceDto> = service.list(q, unitId, pageable)

  @GetMapping("/{code}") fun get(@PathVariable code: String): ProvinceDto = service.get(code)

  @GetMapping("/{code}/wards")
  fun wards(
    @PathVariable code: String,
    @RequestParam(required = false) q: String?,
    @PageableDefault(size = 20, sort = ["code"]) pageable: Pageable,
  ): Page<WardDto> = service.listWards(code, q, pageable)
}
