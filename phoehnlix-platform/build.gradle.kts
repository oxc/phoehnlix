plugins {
  `java-platform`
}

javaPlatform {
  // dependencies are only allowed to be able to import BOMs.
  // Do not add any library dependencies to the platform!
  allowDependencies()
}

val logback_version: String by project
val kotlin_version: String by project
val kotlinx_coroutines_version: String by project
val kotlinx_html_version: String by project
val kotlinx_serialization_version: String by project
val ktor_version: String by project
val testng_version: String by project
val exposed_version: String by project
val postgresql_version: String by project
val sqlite_version: String by project

dependencies {
  constraints {
    api(project(":phoehnlix-util"))
    api(project(":phoehnlix-common"))
    api(project(":phoehnlix-api"))
    api(project(":phoehnlix-apiclient"))
    api(project(":phoehnlix-apiservice"))
    api(project(":phoehnlix-dataservice"))
    api(project(":phoehnlix-frontend"))
    api(project(":phoehnlix-full"))

    api("ch.qos.logback:logback-classic:$logback_version")

    api("org.jetbrains.exposed:exposed-core:$exposed_version")
    api("org.jetbrains.exposed:exposed-dao:$exposed_version")
    api("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    api("org.jetbrains.exposed:exposed-java-time:$exposed_version")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jdk8:$kotlinx_coroutines_version")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$kotlinx_coroutines_version")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$kotlinx_coroutines_version")

    api("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinx_serialization_version")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json-js:$kotlinx_serialization_version")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json-common:$kotlinx_serialization_version")

    api("org.jetbrains.kotlinx:kotlinx-html:$kotlinx_html_version")
    api("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinx_html_version")
    api("org.jetbrains.kotlinx:kotlinx-html-common:$kotlinx_html_version")

    api("com.github.doyaaaaaken:kotlin-csv-jvm:0.7.3")

    api("commons-codec:commons-codec:1.14")

    api("org.postgresql:postgresql:$postgresql_version")
    api("org.xerial:sqlite-jdbc:$sqlite_version")

    api("org.testng:testng:$testng_version")
  }

  api(platform("org.jetbrains.kotlin:kotlin-bom:$kotlin_version"))
  api(platform("io.ktor:ktor-bom:$ktor_version"))

}

