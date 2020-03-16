val logback_version: String by project
val kotlin_version: String by project
val ktor_version: String by project
val testng_version: String by project
val exposed_version: String by project
val postgresql_version: String? by project

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
    val jvmMain by getting {
      dependencies {
        implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
        implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
        implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
        implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
        implementation("io.ktor:ktor-server-core:$ktor_version")

        postgresql_version?.takeIf { it.isNotBlank() }?.let {
          runtimeOnly("org.postgresql:postgresql:$it")
        }
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
