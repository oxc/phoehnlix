val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project

plugins {
  application
  kotlin("jvm")
  id("com.github.johnrengelman.shadow")
}

group = "de.esotechnik.phoehnlix"
version = "0.0.1-SNAPSHOT"

application {
  mainClassName = "io.ktor.server.netty.EngineMain"
}

repositories {
  mavenLocal()
  jcenter()
  maven { url = uri("https://kotlin.bintray.com/ktor") }
}

dependencies {
  implementation(enforcedPlatform(project(":phoehnlix-platform")))

  implementation(project(":phoehnlix-common"))
  api(project(":phoehnlix-api"))
  implementation(project(":phoehnlix-apiclient"))
  implementation(project(":phoehnlix-util"))

  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("io.ktor:ktor-server-netty")
  implementation("ch.qos.logback:logback-classic")
  implementation("io.ktor:ktor-server-core")
  implementation("io.ktor:ktor-client-apache")
  implementation("io.ktor:ktor-client-logging-jvm")
  testImplementation("io.ktor:ktor-server-tests")
}
