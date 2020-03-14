val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project

plugins {
  application
  kotlin("jvm")
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
  implementation(project(":phoehnlix-api"))

  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
  implementation("io.ktor:ktor-server-netty:$ktor_version")
  implementation("ch.qos.logback:logback-classic:$logback_version")
  implementation("io.ktor:ktor-server-core:$ktor_version")
  testImplementation("io.ktor:ktor-server-tests:$ktor_version")
}
