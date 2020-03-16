plugins {
  kotlin("multiplatform") apply false
  kotlin("plugin.serialization") apply false
}

group = "de.esotechnik.phoehnlix"
version = "0.0.1-SNAPSHOT"

repositories {
  mavenLocal()
  jcenter()
  maven { url = uri("https://kotlin.bintray.com/ktor") }
}

tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
}

allprojects {
  afterEvaluate {
    val CONFIG_NAMES = listOf("Api", "Implementation", "RuntimeOnly")
    dependencies {
      configurations.all {
        name.capitalize().let { cname ->
          if ((cname in CONFIG_NAMES || CONFIG_NAMES.any { cname.endsWith(it) }) && "Metadata" !in cname) {
            dependencies.add(enforcedPlatform(project(":phoehnlix-platform")))
          }
        }
      }
    }
  }
}