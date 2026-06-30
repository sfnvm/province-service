package com.sfnvm.province.web

import com.sfnvm.province.service.WardService
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
@RequestMapping("/api/v1/wards")
class WardController(private val service: WardService) {

  @GetMapping
  fun list(
    @RequestParam(required = false) q: String?,
    @RequestParam(required = false) provinceCode: String?,
    @RequestParam(required = false) unitId: Int?,
    @PageableDefault(size = 20, sort = ["code"]) pageable: Pageable,
  ): Page<WardDto> = service.list(q, provinceCode, unitId, pageable)

  @GetMapping("/{code}") fun get(@PathVariable code: String): WardDto = service.get(code)
}
