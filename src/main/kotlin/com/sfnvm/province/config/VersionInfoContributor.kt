package com.sfnvm.province.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component

/**
 * Surfaces the service version and build commit at `/actuator/info`. This avoids the Gradle
 * build-info packaging, which is unreliable with the layered thin-jar image build. The version is
 * read from the jar manifest's `Implementation-Version` (the same value Spring logs at startup);
 * the commit comes from the `GIT_COMMIT` env baked into the image (CI passes the git sha).
 */
@Component
class VersionInfoContributor(@Value("\${GIT_COMMIT:unknown}") private val commit: String) :
  InfoContributor {

  override fun contribute(builder: Info.Builder) {
    val version = javaClass.`package`?.implementationVersion ?: "unknown"
    builder.withDetail("app", mapOf("name" to "province", "version" to version, "commit" to commit))
  }
}
