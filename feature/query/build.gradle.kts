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
    implementation(project(":core:database"))
    implementation(project(":feature:grid"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(libs.rsyntaxtextarea)
}
