package com.sfnvm.province.web

import com.sfnvm.province.AbstractPostgresIT
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest : AbstractPostgresIT() {

  @Autowired lateinit var mvc: MockMvc

  @Test
  fun `list provinces returns a page with nested unit`() {
    mvc
      .get("/api/v1/provinces") { param("sort", "code") }
      .andExpect {
        status { isOk() }
        jsonPath("$.content[0].code") { value("01") }
        jsonPath("$.content[0].name") { value("Hà Nội") }
        jsonPath("$.content[0].administrativeUnit.id") { value(1) }
        jsonPath("$.page.totalElements") { value(2) }
      }
  }

  @Test
  fun `get province by code`() {
    mvc.get("/api/v1/provinces/01").andExpect {
      status { isOk() }
      jsonPath("$.fullName") { value("Thành phố Hà Nội") }
      jsonPath("$.administrativeUnit.shortName") { value("Thành phố") }
    }
  }

  @Test
  fun `unknown province yields 404 problem detail`() {
    mvc.get("/api/v1/provinces/99").andExpect {
      status { isNotFound() }
      jsonPath("$.title") { value("Resource not found") }
      jsonPath("$.status") { value(404) }
    }
  }

  @Test
  fun `list wards of a province`() {
    mvc
      .get("/api/v1/provinces/01/wards") { param("q", "ngoc") }
      .andExpect {
        status { isOk() }
        jsonPath("$.content[0].code") { value("00008") }
        jsonPath("$.content[0].provinceCode") { value("01") }
        jsonPath("$.page.totalElements") { value(1) }
      }
  }

  @Test
  fun `reference lists`() {
    mvc.get("/api/v1/administrative-units").andExpect {
      status { isOk() }
      jsonPath("$.length()") { value(3) }
    }
    mvc.get("/api/v1/administrative-regions").andExpect {
      status { isOk() }
      jsonPath("$[0].nameEn") { value("Red River Delta") }
    }
  }
}
