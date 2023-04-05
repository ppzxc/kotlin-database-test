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

    // LIBS
//    implementation(libs.spring.boot.r2dbc)
    implementation(libs.password4j)
    implementation(libs.mapstruct)
    kapt(libs.kapt.mapstruct)
    kaptTest(libs.kapt.test.mapstruct)

    // DRIVER
    implementation(libs.org.postgresql)
    implementation(libs.org.postgresql.r2dbc)
    implementation(libs.io.r2dbc.pool)
    implementation(libs.io.r2dbc.spi)

    // TEST
//    testImplementation(Modules.springDataDto)
//    testImplementation(testFixtures(project(Modules.testFixture.name)))
//    testImplementation(libs.spring.boot.test)
    testImplementation(libs.test.project.reactor)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.extension.spring)
    testImplementation(libs.mock.kotlin)

    // CUCUMBER TEST
//    testImplementation(libs.cucumber.java)
//    testImplementation(libs.cucumber.java8)
//    testImplementation(libs.cucumber.spring)
//    testImplementation(libs.cucumber.junit)
//    testImplementation(libs.cucumber.engine)
}
