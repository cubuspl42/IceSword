plugins {
    kotlin("js") version "1.5.30" apply false
}

allprojects {
    group = "me.kuba"
    version = "1.0-SNAPSHOT"

    repositories {
        jcenter()
        mavenCentral()
    }
}
