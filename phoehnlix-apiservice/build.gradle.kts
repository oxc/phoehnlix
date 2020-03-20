plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

repositories {
  mavenLocal()
  jcenter()
  maven { url = uri("https://kotlin.bintray.com/ktor") }
}

kotlin {
  sourceSets {
    jvm()
    js {
      browser {
      }
    }
    val commonMain by getting {
      dependencies {
        api(project(":phoehnlix-common"))

        implementation(kotlin("stdlib-common"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common")
        api("io.ktor:ktor-client-core")
      }
    }
    val jvmMain by getting {
      dependencies {
        api(project(":phoehnlix-common"))

        implementation(kotlin("stdlib-jdk8"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime")

        implementation("io.ktor:ktor-server-netty")
        implementation("ch.qos.logback:logback-classic")
        implementation("io.ktor:ktor-server-core")
        implementation("io.ktor:ktor-serialization")
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
        implementation(project(":phoehnlix-common"))

        implementation(kotlin("stdlib-js"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js")

        implementation("io.ktor:ktor-client-serialization-js")
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