package com.sfnvm.province.web

import com.sfnvm.province.service.ReferenceService
import com.sfnvm.province.web.dto.AdministrativeRegionDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/administrative-regions")
class AdministrativeRegionController(private val service: ReferenceService) {

  @GetMapping fun list(): List<AdministrativeRegionDto> = service.listRegions()

  @GetMapping("/{id}")
  fun get(@PathVariable id: Int): AdministrativeRegionDto = service.getRegion(id)
}
