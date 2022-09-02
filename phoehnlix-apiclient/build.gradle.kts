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
        api(project(":phoehnlix-api"))

        implementation(kotlin("stdlib-common"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
        api("io.ktor:ktor-client-core")
        implementation("io.ktor:ktor-client-content-negotiation")
        implementation("io.ktor:ktor-serialization-kotlinx-json")
        implementation("io.ktor:ktor-client-json")
        implementation("io.ktor:ktor-client-serialization")
      }
    }
    val jsMain by getting {
      dependencies {
        implementation(project(":phoehnlix-util"))
        api(project(":phoehnlix-api"))

        implementation(kotlin("stdlib-js"))

        implementation("io.ktor:ktor-client-json-js")
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