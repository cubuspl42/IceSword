plugins {
    kotlin("js")
    kotlin("plugin.serialization") version "1.5.31"
}

dependencies {
    implementation(project(":frp"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-css:1.0.0-pre.214-kotlin-1.5.20")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")

    implementation(npm("pako", "2.0.3"))
    implementation(npm("@types/pako", "1.0.2", generateExternals = true))
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
