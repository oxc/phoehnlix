import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

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
      languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
    }
    jvm()
    js {
      useCommonJs()
      browser {
        // https://kotlinlang.org/docs/reference/javascript-dce.html#known-issue-dce-and-ktor
        dceTask {
          keep("ktor-ktor-io.\$\$importsForInline\$\$.ktor-ktor-io.io.ktor.utils.io")
        }
        webpackTask {
          output.libraryTarget = "umd2"
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
        val kotlinWrappersBuild = "pre.301-kotlin-$kotlin_version"

        implementation(project(":phoehnlix-util"))
        implementation(project(":phoehnlix-apiclient"))

        implementation(kotlin("stdlib-js"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions:1.0.1-$kotlinWrappersBuild")

        implementation("org.jetbrains.kotlinx:kotlinx-html-js")
        implementation("io.ktor:ktor-client-js")

        val reactVersion = "17.0.2"
        val reactRouterVersion = "6.2.1"
        implementation("org.jetbrains.kotlin-wrappers:kotlin-react:$reactVersion-$kotlinWrappersBuild")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-react-legacy:$reactVersion-$kotlinWrappersBuild")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom-legacy:$reactVersion-$kotlinWrappersBuild")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:$reactRouterVersion-$kotlinWrappersBuild")

        implementation("org.jetbrains.kotlin-wrappers:kotlin-css-js:1.0.0-$kotlinWrappersBuild")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.3-$kotlinWrappersBuild")

        val kotlinMuiVersion = "0.7.0-oxc"
        implementation("net.subroh0508.kotlinmaterialui:core:$kotlinMuiVersion")
        implementation("net.subroh0508.kotlinmaterialui:lab:$kotlinMuiVersion")

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
