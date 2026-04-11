import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
}

configure<LibraryExtension> {
    namespace = "com.laguipemo.nefroped.di"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Conoce todos los módulos para inyectarlos
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":local"))
    implementation(project(":auth"))
    implementation(project(":chat"))
    implementation(project(":onboarding"))
    implementation(project(":profile"))
    implementation(project(":course"))
    implementation(project(":notifications"))
    implementation(project(":admin")) // Nuevo módulo de administración añadido

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
}
