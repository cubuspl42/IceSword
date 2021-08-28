plugins {
    kotlin("js")
}

dependencies {
    implementation(npm("icesword-frp", fileDep("icesword-frp")))

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
