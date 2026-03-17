pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        //add itpack.io for compose markdown
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "NefroPed"
include(":app")

// Nuevos módulos
include(":domain")
include(":common")
include(":local")
include(":data")
include(":auth")
include(":designsystem")
include(":di")
include(":onboarding")
include(":chat")
include(":profile")
include(":navigation")
include(":course")
