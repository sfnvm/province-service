package com.sfnvm.province.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "administrative_units")
class AdministrativeUnit(
  @Id var id: Int,
  @Column(name = "full_name") var fullName: String?,
  @Column(name = "full_name_en") var fullNameEn: String?,
  @Column(name = "short_name") var shortName: String?,
  @Column(name = "short_name_en") var shortNameEn: String?,
  @Column(name = "code_name") var codeName: String?,
  @Column(name = "code_name_en") var codeNameEn: String?,
)
