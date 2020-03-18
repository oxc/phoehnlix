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
