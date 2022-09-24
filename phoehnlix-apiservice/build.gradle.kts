import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  application
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("com.github.johnrengelman.shadow")
}

repositories {
  mavenLocal()
  mavenCentral()
}

application {
  mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
  implementation(enforcedPlatform(project(":phoehnlix-platform")))

  implementation(project(":phoehnlix-util"))
  implementation(project(":phoehnlix-common"))
  api(project(":phoehnlix-api"))

  implementation(kotlin("stdlib-jdk8"))
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")

  implementation("io.ktor:ktor-server-netty")
  implementation("ch.qos.logback:logback-classic")
  implementation("io.ktor:ktor-server-core")
  implementation("io.ktor:ktor-server-cors")
  implementation("io.ktor:ktor-server-forwarded-header")
  implementation("io.ktor:ktor-server-status-pages")
  implementation("io.ktor:ktor-server-content-negotiation")
  implementation("io.ktor:ktor-server-call-logging")
  implementation("io.ktor:ktor-serialization-kotlinx-json")
  implementation("io.ktor:ktor-server-auth")
  implementation("io.ktor:ktor-server-auth-jwt")
  implementation("io.ktor:ktor-client-apache")
  implementation("io.ktor:ktor-client-json")
  implementation("io.ktor:ktor-client-content-negotiation")
  implementation("io.ktor:ktor-client-logging")
  implementation("commons-codec:commons-codec")

  api("org.jetbrains.exposed:exposed-core")
  api("org.jetbrains.exposed:exposed-dao")
  implementation("org.jetbrains.exposed:exposed-jdbc")
  implementation("org.jetbrains.exposed:exposed-java-time")

  runtimeOnly("org.postgresql:postgresql")
  runtimeOnly("org.xerial:sqlite-jdbc")

  testImplementation("io.ktor:ktor-server-tests")
}

tasks.withType<KotlinCompile>().all {
  kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}