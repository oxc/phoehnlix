import io.ktor.plugin.features.DockerImageRegistry

plugins {
  application
  kotlin("jvm")
  id("io.ktor.plugin")
}

group = "de.esotechnik.phoehnlix"
version = "0.0.1-SNAPSHOT"

application {
  mainClass.set("io.ktor.server.netty.EngineMain")
}
jib {
  container {
    mainClass = "io.ktor.server.netty.EngineMain"
    args = listOf("-config=/etc/phoehnlix/phoehnlix.conf")
  }

  from {
    platforms {
      platform {
        architecture = "arm64"
        os = "linux"
      }
    }
  }
}


ktor {
  docker {
    jreVersion.set(io.ktor.plugin.features.JreVersion.JRE_17)
    externalRegistry.set(
      DockerImageRegistry.dockerHub(
        appName = provider { "phoehnlix" },
        username = providers.environmentVariable("DOCKER_HUB_USERNAME"),
        password = providers.environmentVariable("DOCKER_HUB_PASSWORD")
      )
    )
    imageTag.set("latest")

    localImageName.set("phoehnlix-docker-image")
  }
}

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  api(enforcedPlatform(project(":phoehnlix-platform")))

  implementation(project(":phoehnlix-dataservice"))
  implementation(project(":phoehnlix-apiservice"))
  implementation(project(":phoehnlix-frontend"))

  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("io.ktor:ktor-server-netty")
  implementation("ch.qos.logback:logback-classic")
  implementation("io.ktor:ktor-server-core")
  testImplementation("io.ktor:ktor-server-tests")
}
