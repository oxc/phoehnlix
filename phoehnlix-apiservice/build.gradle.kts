import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  application
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("com.github.johnrengelman.shadow")
}

repositories {
  mavenLocal()
  jcenter()
  maven { url = uri("https://kotlin.bintray.com/ktor") }
}

application {
  mainClassName = "io.ktor.server.netty.EngineMain"
}

dependencies {
  implementation(enforcedPlatform(project(":phoehnlix-platform")))

  implementation(project(":phoehnlix-util"))
  implementation(project(":phoehnlix-common"))
  api(project(":phoehnlix-api"))

  implementation(kotlin("stdlib-jdk8"))
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime")

  implementation("io.ktor:ktor-server-netty")
  implementation("ch.qos.logback:logback-classic")
  implementation("io.ktor:ktor-server-core")
  implementation("io.ktor:ktor-serialization")
  implementation("io.ktor:ktor-auth")
  implementation("io.ktor:ktor-auth-jwt")
  implementation("io.ktor:ktor-client-apache")
  implementation("io.ktor:ktor-client-json-jvm")
  implementation("io.ktor:ktor-client-serialization-jvm")
  implementation("io.ktor:ktor-client-logging-jvm")
  implementation("commons-codec:commons-codec")

  api("org.jetbrains.exposed:exposed-core")
  api("org.jetbrains.exposed:exposed-dao")
  implementation("org.jetbrains.exposed:exposed-jdbc")
  implementation("org.jetbrains.exposed:exposed-java-time")
  implementation("io.ktor:ktor-server-core")

  runtimeOnly("org.postgresql:postgresql")

  testImplementation("io.ktor:ktor-server-tests")
}

tasks.withType<KotlinCompile>().all {
  kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}