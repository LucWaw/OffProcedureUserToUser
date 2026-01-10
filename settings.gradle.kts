// The settings file is the entry point of every Gradle build.
// Its primary purpose is to define the subprojects.
// It is also used for some aspects of project-wide configuration, like managing plugins, dependencies, etc.
// https://docs.gradle.org/current/userguide/settings_file_basics.html

dependencyResolutionManagement {
    // Use Maven Central as the default repository (where Gradle will download dependencies) in all subprojects.
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        google()
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

plugins {
    // Use the Foojay Toolchains plugin to automatically download JDKs required by subprojects.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

// If there are changes in only one of the projects, Gradle will rebuild only the one that has changed.
// when running the assemble task, ignore the android & graalvm related subprojects
if (startParameter.taskRequests.find { it.args.contains("assemble") } == null) {
    include(
        "protos",
        "stub",
        "server",
        "stub-android",
        "android"
    )
} else {
    include("protos", "server", "stub")
}

rootProject.name = "OffProcedureUserToUser"