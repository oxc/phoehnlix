import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
  kotlin("multiplatform")
}

group = "de.esotechnik.phoehnlix"
version = "0.0.1-SNAPSHOT"

repositories {
  mavenLocal()
  jcenter()
  maven { url = uri("https://kotlin.bintray.com/ktor") }
  maven { url = uri("https://kotlin.bintray.com/kotlin-js-wrappers") }
}

kotlin {
  sourceSets {
    jvm()
    js {
      useCommonJs()
      browser {
        webpackTask {
          output.libraryTarget = "umd2"
          report = true
        }
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(kotlin("stdlib-jdk8"))
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
        api(project(":phoehnlix-apiservice"))
        api(project(":phoehnlix-common"))

        implementation(kotlin("stdlib-js"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js")

        //implementation("org.jetbrains.kotlinx:kotlinx-html-js")
        implementation("io.ktor:ktor-client-js")
        implementation("io.ktor:ktor-client-json-js")
        implementation("io.ktor:ktor-client-serialization-js")

        implementation("org.jetbrains:kotlin-react:16.13.0-pre.94-kotlin-1.3.70")
        implementation("org.jetbrains:kotlin-react-dom:16.13.0-pre.94-kotlin-1.3.70")
        implementation(npm("react", "16.13.1"))
        implementation(npm("react-dom", "16.13.1"))

        implementation("org.jetbrains:kotlin-styled:1.0.0-pre.94-kotlin-1.3.70")
        implementation(npm("styled-components"))
        implementation(npm("inline-style-prefixer"))

        implementation(npm("chart.js", "2.9.3"))
        implementation(npm("chartjs-adapter-date-fns", "1.0.0"))
        implementation(npm("date-fns", "2.11.0"))
        implementation(npm("chartjs-plugin-downsample", "1.1.0"))
      }
    }
  }
}

val jvmProcessResources by tasks.withType(ProcessResources::class).getting {
    // val jsWebpackTaskName = "jsBrowserProductionWebpack"
    val jsWebpackTaskName = "jsBrowserDevelopmentWebpack"
    // should we also choose "jsBrowserDevelopmentWebpack"? Probably should yield a different jar.
    val jsWebpackTask = tasks.withType(KotlinWebpack::class).named(jsWebpackTaskName)
    from(jsWebpackTask.map { project.files(it.destinationDirectory) }) {
      into("de/esotechnik/phoehnlix/frontend/static/js")
    }
}


dependencies {
  configurations.all {
    if (name.endsWith("MainImplementation")
      || name.endsWith("MainApi")
      || name.endsWith("MainRuntimeOnly")) {
      dependencies.add(enforcedPlatform(project(":phoehnlix-platform")))
    }
  }
}
