pluginManagement {
  val kotlin_version: String by settings 

  repositories {
    gradlePluginPortal()
  }
  plugins {
    kotlin("jvm") version kotlin_version
    kotlin("plugin.serialization") version kotlin_version
  }
  resolutionStrategy {
    eachPlugin {
      when (requested.id.id) {
        "org.jetbrains.kotlin.plugin.serialization" -> useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version ?: kotlin_version}")
      }
    }
  }
}


