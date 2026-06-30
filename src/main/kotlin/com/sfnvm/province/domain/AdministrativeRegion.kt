package com.sfnvm.province.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "administrative_regions")
class AdministrativeRegion(
  @Id var id: Int,
  @Column(name = "name") var name: String,
  @Column(name = "name_en") var nameEn: String,
  @Column(name = "code_name") var codeName: String?,
  @Column(name = "code_name_en") var codeNameEn: String?,
)
