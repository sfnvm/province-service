package com.sfnvm.province.web

import com.sfnvm.province.service.ReferenceService
import com.sfnvm.province.web.dto.AdministrativeUnitDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/administrative-units")
class AdministrativeUnitController(private val service: ReferenceService) {

  @GetMapping fun list(): List<AdministrativeUnitDto> = service.listUnits()

  @GetMapping("/{id}") fun get(@PathVariable id: Int): AdministrativeUnitDto = service.getUnit(id)
}
