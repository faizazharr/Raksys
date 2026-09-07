plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:security"))
    implementation(project(":core:database"))
    implementation(project(":feature:connection"))
    implementation(project(":feature:navigator"))
    implementation(project(":feature:grid"))
    implementation(project(":feature:query"))
    implementation(project(":feature:document"))
    implementation(project(":feature:keyvalue"))
    implementation(project(":feature:permission"))
    implementation(project(":feature:erd"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
}

compose.desktop {
    application {
        mainClass = "com.raksys.app.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
            )
            packageName = "Raksys"
            packageVersion = "1.0.0"
        }
    }
}
