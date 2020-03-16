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
  implementation(project(":phoehnlix-dataservice"))
  implementation(project(":phoehnlix-apiservice"))
  implementation(project(":phoehnlix-frontend"))

  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("io.ktor:ktor-server-netty")
  implementation("ch.qos.logback:logback-classic")
  implementation("io.ktor:ktor-server-core")
  testImplementation("io.ktor:ktor-server-tests")
}
