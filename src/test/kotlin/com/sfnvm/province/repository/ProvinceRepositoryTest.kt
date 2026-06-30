package com.sfnvm.province.repository

import com.sfnvm.province.AbstractPostgresIT
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.data.domain.PageRequest

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProvinceRepositoryTest : AbstractPostgresIT() {

  @Autowired lateinit var provinces: ProvinceRepository

  private val page = PageRequest.of(0, 10)

  @Test
  fun `search matches Unicode name`() {
    val result = provinces.search("%hà nội%", null, page)
    assertEquals(listOf("01"), result.content.map { it.code })
  }

  @Test
  fun `search via ASCII code_name is accent-insensitive`() {
    val result = provinces.search("%ha_noi%", null, page)
    assertEquals(1, result.totalElements)
    assertEquals("01", result.content.single().code)
  }

  @Test
  fun `search filters by unit id`() {
    val result = provinces.search(null, 2, page)
    assertEquals(listOf("04"), result.content.map { it.code })
  }

  @Test
  fun `findByCode eagerly loads nested administrative unit`() {
    val province = provinces.findByCode("01")
    assertNotNull(province)
    assertEquals(1, province.administrativeUnit?.id)
    assertEquals("Thành phố", province.administrativeUnit?.shortName)
  }

  @Test
  fun `blank-ish search returns all`() {
    val result = provinces.search(null, null, page)
    assertTrue(result.totalElements >= 2)
  }
}
