enableFeaturePreview("GRADLE_METADATA")

pluginManagement {
  val kotlin_version: String by settings
  val shadow_version: String by settings

  repositories {
    gradlePluginPortal()
  }
  plugins {
    kotlin("jvm") version kotlin_version
    kotlin("multiplatform") version kotlin_version
    kotlin("plugin.serialization") version kotlin_version
    id("com.github.johnrengelman.shadow") version shadow_version
  }
  resolutionStrategy {
    eachPlugin {
      when (requested.id.id) {
        "org.jetbrains.kotlin.plugin.serialization" -> useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version ?: kotlin_version}")
      }
    }
  }
}

rootProject.name = "phoehnlix"

include("phoehnlix-platform")
include("phoehnlix-common")
include("phoehnlix-dataservice")
include("phoehnlix-apiservice")
include("phoehnlix-frontend")
include("phoehnlix-full")
