plugins {
    kotlin("js") version "1.5.30"
}

group = "me.kuba"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-css:1.0.0-pre.214-kotlin-1.5.20")

    implementation(npm("pako", "2.0.3"))
    implementation(npm("@types/pako", "1.0.2", generateExternals = true))

    testImplementation(kotlin("test"))
}

kotlin {
    js(LEGACY) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
}