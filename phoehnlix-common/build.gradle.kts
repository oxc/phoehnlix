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
      }
    }

    val commonMain by getting {
      dependencies {
        implementation(kotlin("stdlib-common"))
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(kotlin("stdlib-jdk8"))

        api("org.jetbrains.exposed:exposed-core")
        api("org.jetbrains.exposed:exposed-dao")
        implementation("org.jetbrains.exposed:exposed-jdbc")
        implementation("org.jetbrains.exposed:exposed-java-time")
        implementation("io.ktor:ktor-server-core")

        runtimeOnly("org.postgresql:postgresql")
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation("org.jetbrains.kotlin:kotlin-test")
        implementation("org.jetbrains.kotlin:kotlin-test-testng")
        implementation("org.testng:testng")
      }
    }
    val jsMain by getting {
      dependencies {
        implementation(kotlin("stdlib-js"))
      }
    }
  }
}

dependencies {
  commonMainApi(enforcedPlatform(project(":phoehnlix-platform")))
  "jsMainApi"(enforcedPlatform(project(":phoehnlix-platform")))
  "jvmMainApi"(enforcedPlatform(project(":phoehnlix-platform")))
}