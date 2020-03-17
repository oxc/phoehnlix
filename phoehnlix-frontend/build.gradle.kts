import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput.Target.COMMONJS

plugins {
  kotlin("multiplatform")
  kotlin("kapt")
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
          output.libraryTarget = COMMONJS
        }
      }
    }
    val commonMain by getting {
      dependencies {
        implementation(project(":phoehnlix-common"))

        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("ch.qos.logback:logback-classic")
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
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
        implementation(project(":phoehnlix-common"))
        implementation(project(":phoehnlix-apiservice"))

        implementation(npm("chart.js", "2.9.3"))
      }
    }
  }
}