@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.spring.boot)
    kotlin("plugin.spring")
    kotlin("kapt")
}

apply(plugin = "org.springframework.boot")
apply(plugin = "kotlin-spring")

coverage {
    exclude(project)
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

sourceSets {
    test {
        resources {
            srcDir(project.files("${project.rootProject.projectDir}/config"))
        }
    }
}

dependencies {
    // DEFAULTS
    implementation(libs.kotlin)
    implementation(libs.kotlin.reflect)
    implementation(libs.com.fasterxml.jackson.kotlin)

    // MODULES
//    implementation(Modules.springDataDto)
//    implementation(Modules.springUtility)
//    implementation(Modules.springSecurityJwt)
//    implementation(Modules.commonExceptions)
//    implementation(Modules.commonUtility)

    // SPRING
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.reactive.couchbase)
    annotationProcessor(libs.spring.boot.configuration.processor)

    // LIB
    implementation(libs.mapstruct)
    kapt(libs.kapt.mapstruct)
    kaptTest(libs.kapt.test.mapstruct)

    // TEST
//    testImplementation(Modules.springDataDto)
//    testImplementation(testFixtures(project(Modules.testFixture.name)))
//    testImplementation(testFixtures(project(Modules.testFixtureCouchbase.name)))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.test.project.reactor)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.extension.spring)
    testImplementation(libs.mock.kotlin)
}
