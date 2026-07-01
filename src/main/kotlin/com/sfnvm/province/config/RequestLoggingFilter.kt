package com.sfnvm.province.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Emits one structured access-log line per API call (method, path, status, duration). The fields
 * are added to the MDC so the ECS console encoder renders them as queryable JSON keys (visible in
 * Loki), and the trace/span ids already correlate each line to its request. Actuator endpoints are
 * skipped so k8s liveness/readiness probes don't flood the logs.
 */
@Component
class RequestLoggingFilter : OncePerRequestFilter() {

  private val log = LoggerFactory.getLogger("http.access")

  override fun shouldNotFilter(request: HttpServletRequest): Boolean =
    request.requestURI.startsWith("/actuator")

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain,
  ) {
    val startNanos = System.nanoTime()
    try {
      filterChain.doFilter(request, response)
    } finally {
      val durationMs = (System.nanoTime() - startNanos) / 1_000_000
      val query = request.queryString?.let { "?$it" } ?: ""
      MDC.put("method", request.method)
      MDC.put("path", request.requestURI)
      MDC.put("status", response.status.toString())
      MDC.put("durationMs", durationMs.toString())
      MDC.put("clientIp", request.remoteAddr)
      try {
        log.info(
          "{} {}{} -> {} ({} ms)",
          request.method,
          request.requestURI,
          query,
          response.status,
          durationMs,
        )
      } finally {
        listOf("method", "path", "status", "durationMs", "clientIp").forEach(MDC::remove)
      }
    }
  }
}
