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
        api(project(":phoehnlix-apiservice"))
        api(project(":phoehnlix-common"))
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
        implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.94-kotlin-1.3.70")

        //implementation("org.jetbrains.kotlinx:kotlinx-html-js")
        implementation("io.ktor:ktor-client-js")
        implementation("io.ktor:ktor-client-json-js")
        implementation("io.ktor:ktor-client-serialization-js")

        implementation("org.jetbrains:kotlin-react:16.13.0-pre.94-kotlin-1.3.70")
        implementation("org.jetbrains:kotlin-react-dom:16.13.0-pre.94-kotlin-1.3.70")
        implementation(npm("react", "16.13.0"))
        implementation(npm("react-is", "16.13.0"))
        implementation(npm("react-dom", "16.13.0"))

        implementation("org.jetbrains:kotlin-css-js:1.0.0-pre.94-kotlin-1.3.70")

        implementation("subroh0508.net.kotlinmaterialui:core:0.3.3-SNAPSHOT")
        implementation("subroh0508.net.kotlinmaterialui:lab:0.3.0-SNAPSHOT")
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