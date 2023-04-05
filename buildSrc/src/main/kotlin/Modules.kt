import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

data class Module(
    val name: String
)

@Suppress("unused")
object Modules {
    val springDataR2dbc = module(":spring-data-r2dbc")

    private fun module(name: String): Module = Module(name)
}

fun DependencyHandler.api(module: Module): Dependency? =
    add("api", this.project(module.name))

fun DependencyHandler.implementation(module: Module): Dependency? =
    add("implementation", this.project(module.name))

fun DependencyHandler.compileOnly(module: Module): Dependency? =
    add("compileOnly", this.project(module.name))

fun DependencyHandler.testImplementation(module: Module): Dependency? =
    add("testImplementation", this.project(module.name))
