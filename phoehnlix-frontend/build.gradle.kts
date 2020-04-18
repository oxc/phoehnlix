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

val kotlin_version: String by project

kotlin {
  sourceSets {
    all {
      languageSettings.enableLanguageFeature("NewInference")
      languageSettings.useExperimentalAnnotation("kotlin.OptIn")
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
        val kotlinWrappersBuild = "pre.97-kotlin-$kotlin_version"

        implementation(project(":phoehnlix-util"))
        implementation(project(":phoehnlix-apiclient"))

        implementation(kotlin("stdlib-js"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js")
        implementation("org.jetbrains:kotlin-extensions:1.0.1-$kotlinWrappersBuild")

        implementation("org.jetbrains.kotlinx:kotlinx-html-js")
        implementation("io.ktor:ktor-client-js")

        val reactVersion = "16.13.0"
        implementation("org.jetbrains:kotlin-react:$reactVersion-$kotlinWrappersBuild")
        implementation("org.jetbrains:kotlin-react-dom:$reactVersion-$kotlinWrappersBuild")
        implementation("org.jetbrains:kotlin-react-router-dom:4.3.1-$kotlinWrappersBuild-SNAPSHOT")
        implementation(npm("react", "$reactVersion"))
        implementation(npm("react-is", "$reactVersion"))
        implementation(npm("react-dom", "$reactVersion"))
        implementation(npm("react-router-dom", "^5.1.2"))

        implementation("org.jetbrains:kotlin-css-js:1.0.0-$kotlinWrappersBuild")

        val kotlinmaterialuiVersion = "0.3.6-SNAPSHOT"
        implementation("subroh0508.net.kotlinmaterialui:core:$kotlinmaterialuiVersion")
        implementation("subroh0508.net.kotlinmaterialui:lab:$kotlinmaterialuiVersion")
        implementation(npm("@material-ui/core", "^4.9.8"))

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
