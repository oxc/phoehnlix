import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput.Target.UMD2

plugins {
  kotlin("multiplatform")
}

group = "de.esotechnik.phoehnlix"
version = "0.0.1-SNAPSHOT"

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
        webpackTask {
          output.libraryTarget = UMD2
        }
      }
    }
    val commonMain by getting {
      dependencies {
        implementation(project(":phoehnlix-common"))
        implementation(project(":phoehnlix-apiservice"))

        implementation(kotlin("stdlib-common"))
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation("io.ktor:ktor-server-netty")
        implementation("ch.qos.logback:logback-classic")
        implementation("io.ktor:ktor-server-core")
        implementation("io.ktor:ktor-html-builder")
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
        api(project(":phoehnlix-apiservice"))

        api(kotlin("stdlib-js"))
        //implementation("org.jetbrains.kotlinx:kotlinx-html-js")

        implementation(npm("require", "2.4.20"))
        implementation(npm("chart.js", "2.9.3"))
      }
    }
  }
}

dependencies {
  commonMainImplementation(enforcedPlatform(project(":phoehnlix-platform")))
  "jsMainImplementation"(enforcedPlatform(project(":phoehnlix-platform")))
  "jvmMainImplementation"(enforcedPlatform(project(":phoehnlix-platform")))
}