package com.sfnvm.province.web.error

/** Thrown when a resource lookup by id/code finds nothing; mapped to HTTP 404. */
class ResourceNotFoundException(resource: String, id: Any) :
  RuntimeException("$resource '$id' not found")
