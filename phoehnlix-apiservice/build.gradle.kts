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
      browser {}
    }
    val commonMain by getting {
      dependencies {
        api(project(":phoehnlix-common"))

        implementation(kotlin("stdlib-common"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common")
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
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation("io.ktor:ktor-server-tests")
      }
    }
    val jsMain by getting {
      dependencies {
        api(project(":phoehnlix-common"))

        implementation(kotlin("stdlib-js"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js")

        implementation("io.ktor:ktor-client-json-js")
      }
    }
  }
}

dependencies {
  commonMainImplementation(enforcedPlatform(project(":phoehnlix-platform")))
  "jsMainImplementation"(enforcedPlatform(project(":phoehnlix-platform")))
  "jvmMainImplementation"(enforcedPlatform(project(":phoehnlix-platform")))
}