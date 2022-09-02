import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput.Target

plugins {
  kotlin("multiplatform")
}

group = "de.esotechnik.phoehnlix"
version = "0.0.1-SNAPSHOT"

repositories {
  mavenLocal()
  mavenCentral()
  maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
}

val kotlin_version: String by project

kotlin {
  sourceSets {
    all {
      languageSettings.enableLanguageFeature("NewInference")
      languageSettings.optIn("kotlin.RequiresOptIn")
    }
    jvm()
    js(IR) {
      binaries.executable()
      useCommonJs()
      browser {
        commonWebpackConfig {
        }
        webpackTask {
          output.libraryTarget = Target.UMD2
          report = true
        }
      }
    }
    val commonMain by getting {
      dependencies {
        implementation(project(":phoehnlix-util"))
        implementation(project(":phoehnlix-api"))
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(project(":phoehnlix-common"))

        implementation(kotlin("stdlib-jdk8"))
        implementation("io.ktor:ktor-server-netty")
        implementation("ch.qos.logback:logback-classic")
        implementation("io.ktor:ktor-server-core")
        implementation("io.ktor:ktor-server-html-builder")
        implementation("io.ktor:ktor-server-call-logging")
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation("io.ktor:ktor-server-tests")
      }
    }
    val jsMain by getting {
      dependencies {
        val kotlinWrappersBuild = "pre.381"

        implementation(project(":phoehnlix-util"))
        implementation(project(":phoehnlix-apiclient"))

        implementation(kotlin("stdlib-js"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions:1.0.1-$kotlinWrappersBuild")

        implementation("org.jetbrains.kotlinx:kotlinx-html-js")
        implementation("io.ktor:ktor-client-js")

        val reactVersion = "18.2.0"
        val reactRouterVersion = "6.3.0"
        implementation("org.jetbrains.kotlin-wrappers:kotlin-react:$reactVersion-$kotlinWrappersBuild")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-react-legacy:$reactVersion-$kotlinWrappersBuild")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom-legacy:$reactVersion-$kotlinWrappersBuild")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:$reactRouterVersion-$kotlinWrappersBuild")

        implementation("org.jetbrains.kotlin-wrappers:kotlin-mui:5.9.1-$kotlinWrappersBuild")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-mui-icons:5.8.4-$kotlinWrappersBuild")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.10.0-$kotlinWrappersBuild")

        implementation("org.jetbrains.kotlin-wrappers:kotlin-css-js:1.0.0-$kotlinWrappersBuild")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.5-$kotlinWrappersBuild")

        implementation(npm("chart.js", "^2.9.3"))
        implementation(npm("chartjs-adapter-date-fns", "^1.0.0"))
        implementation(npm("date-fns", "^2.11.0"))
        implementation(npm("chartjs-plugin-downsample", "^1.1.0"))
      }
    }
  }
}

val jvmProcessResources by tasks.withType(ProcessResources::class).getting {
    val jsWebpackTaskName = "jsBrowserProductionWebpack"
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
