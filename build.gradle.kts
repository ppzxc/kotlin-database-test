import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    jacoco

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.noarg)
    alias(libs.plugins.kotlin.allopen)
}

allprojects {
    group = "com.nanoit"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

rootProject {
    apply<JacocoExtensionPlugin>()
}

subprojects {
    apply(plugin = "jacoco")
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-noarg")
    apply(plugin = "kotlin-allopen")

    apply<LocalPropertiesPlugin>()

    dependencies {
        implementation(rootProject.libs.kotlin)
    }

    allOpen {

    }

    noArg {

    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            showExceptions = true
            exceptionFormat = FULL
            showCauses = true
            showStackTraces = true
            events = setOf(FAILED)
        }
    }

    kotlin {
        jvmToolchain(17)
    }
}