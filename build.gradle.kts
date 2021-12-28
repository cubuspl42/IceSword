plugins {
    kotlin("js") version "1.6.10" apply false
    kotlin("jvm") version "1.6.10" apply false
}

allprojects {
    group = "me.kuba"
    version = "1.0-SNAPSHOT"

    repositories {
        jcenter()
        mavenCentral()
    }
}
