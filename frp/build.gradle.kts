plugins {
    kotlin("js")
}

dependencies {
    implementation(npm("frp-js", fileDep("frp-js")))

    testImplementation(kotlin("test"))
}

kotlin {
    js(LEGACY) {
        binaries.executable()

        nodejs {
        }
    }
}

fun fileDep(relativePath: String): String {
    val absolutePath = rootProject.projectDir.resolve(relativePath).canonicalPath
    return "file:$absolutePath"
}
