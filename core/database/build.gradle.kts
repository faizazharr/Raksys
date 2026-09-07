plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:security"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.hikaricp)
    implementation(libs.postgresql)
    implementation(libs.mysql.connector)
    implementation(libs.sqlite.jdbc)
    implementation(libs.sshj)
    implementation(libs.mongodb.driver.sync)
    implementation(libs.jedis)
    implementation(libs.koin.core)
    testImplementation(libs.kotlin.test.junit5)
}

tasks.test {
    useJUnitPlatform()
}
