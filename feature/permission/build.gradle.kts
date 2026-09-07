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
    implementation(project(":core:database"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
}
