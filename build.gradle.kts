plugins {
  kotlin("multiplatform") apply false
  kotlin("plugin.serialization") apply false
}

group = "de.esotechnik.phoehnlix"
version = "0.0.1-SNAPSHOT"

repositories {
  mavenLocal()
  mavenCentral()
}

tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
}
