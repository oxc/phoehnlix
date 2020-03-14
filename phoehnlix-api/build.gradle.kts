val logback_version: String by project
val kotlin_version: String by project
val testng_version: String by project

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
    val commonMain by getting {
      dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
        implementation("ch.qos.logback:logback-classic:$logback_version")
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
        implementation("org.jetbrains.kotlin:kotlin-test-testng:$kotlin_version")
        implementation("org.testng:testng:$testng_version")
      }
    }

  }
}
