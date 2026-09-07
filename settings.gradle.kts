rootProject.name = "raksys"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(
    ":app",
    ":core:model",
    ":core:security",
    ":core:database",
    ":feature:connection",
    ":feature:navigator",
    ":feature:grid",
    ":feature:query",
)
