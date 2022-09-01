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
}

rootProject.name = "phoehnlix"

include("phoehnlix-platform")
include("phoehnlix-util")
include("phoehnlix-common")
include("phoehnlix-api")
include("phoehnlix-apiclient")
include("phoehnlix-apiservice")
include("phoehnlix-dataservice")
include("phoehnlix-frontend")
include("phoehnlix-full")
