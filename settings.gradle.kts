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
    }
}

rootProject.name = "NefroPed"
include(":app")

// Nuevos módulos
include(":core:domain")
include(":core:data")
include(":core:local")
include(":core:common")
include(":features:auth")
include(":features:chat")
include(":features:courses")
include(":features:onboarding")
include(":features:splash")
include(":designsystem")
include(":di")
include(":domain")
include(":domain")
