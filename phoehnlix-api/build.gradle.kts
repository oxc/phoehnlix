plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

repositories {
  mavenLocal()
  mavenCentral()
}

kotlin {
  sourceSets {
    jvm()
    js(IR) {
      browser {
      }
    }
    val commonMain by getting {
      dependencies {
        api(project(":phoehnlix-util"))

        implementation(kotlin("stdlib-common"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
        api("io.ktor:ktor-client-core")
        implementation("io.ktor:ktor-client-json")
        implementation("io.ktor:ktor-serialization-kotlinx-json")
      }
    }
    val jvmMain by getting {
      dependencies {
        api(project(":phoehnlix-util"))

        implementation(kotlin("stdlib-jdk8"))

        implementation("io.ktor:ktor-server-netty")
        implementation("ch.qos.logback:logback-classic")
        implementation("io.ktor:ktor-server-core")
        implementation("io.ktor:ktor-client-json-jvm")
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation("io.ktor:ktor-server-tests")
      }
    }
    val jsMain by getting {
      dependencies {
        implementation(project(":phoehnlix-util"))

        implementation(kotlin("stdlib-js"))

        implementation("io.ktor:ktor-client-json-js")
      }
    }
  }
}

dependencies {
  configurations.all {
    if (name.endsWith("MainImplementation")
      || name.endsWith("MainApi")
      || name.endsWith("MainRuntimeOnly")) {
      dependencies.add(enforcedPlatform(project(":phoehnlix-platform")))
    }
  }
}