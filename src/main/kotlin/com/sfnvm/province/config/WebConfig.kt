package com.sfnvm.province.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer

/** Serialize `Page` via a stable DTO shape instead of the unstable PageImpl JSON. */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
class WebConfig {

  /**
   * Cap the page size. The `spring.data.web.pageable.*` properties are ignored once
   *
   * @EnableSpringDataWebSupport is present, so set the limit on the resolver directly.
   */
  @Bean
  fun pageableCustomizer() = PageableHandlerMethodArgumentResolverCustomizer { resolver ->
    resolver.setMaxPageSize(200)
  }
}
