rootProject.name = "kotlin-database-test"

val modules: MutableList<Module> = mutableListOf()

fun module(name: String, path: String) {
    modules.add(Module(name, "${rootDir}/${path}"))
}

data class Module(
    val name: String,
    val path: String,
)

module(name = ":spring-data-couchbase", path = "spring-data-couchbase")
module(name = ":spring-data-r2dbc", path = "spring-data-r2dbc")
module(name = ":spring-data-scylladb", path = "spring-data-scylladb")

modules.forEach {
    include(it.name)
    project(it.name).projectDir = file(it.path)
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
