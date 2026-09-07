plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.hikaricp)
    implementation(libs.postgresql)
    implementation(libs.mysql.connector)
    implementation(libs.sqlite.jdbc)
}
