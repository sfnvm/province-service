package com.sfnvm.province.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "wards")
class Ward(
  @Id var code: String,
  @Column(name = "name") var name: String,
  @Column(name = "name_en") var nameEn: String?,
  @Column(name = "full_name") var fullName: String?,
  @Column(name = "full_name_en") var fullNameEn: String?,
  @Column(name = "code_name") var codeName: String?,
  @Column(name = "province_code") var provinceCode: String?,
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "administrative_unit_id")
  var administrativeUnit: AdministrativeUnit?,
)
