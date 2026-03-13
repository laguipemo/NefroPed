plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    // Koin core
    implementation(libs.koin.core)

    // Coroutines para Flow y suspend functions
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
}
