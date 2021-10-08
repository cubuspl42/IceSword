plugins {
    kotlin("js") version "1.5.31" apply false
}

allprojects {
    group = "me.kuba"
    version = "1.0-SNAPSHOT"

    repositories {
        jcenter()
        mavenCentral()
    }
}

// Workaround for https://youtrack.jetbrains.com/issue/KT-49124
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>().apply {
        resolution("@webpack-cli/serve", "1.5.2")
    }
}
