rootProject.name = "raksys"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Auto-provisions a JetBrains Runtime (JBR) so Compose Hot Reload has the runtime it needs.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
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
    ":core:ui",
    ":feature:connection",
    ":feature:navigator",
    ":feature:grid",
    ":feature:query",
    ":feature:document",
    ":feature:keyvalue",
    ":feature:permission",
    ":feature:erd",
)
