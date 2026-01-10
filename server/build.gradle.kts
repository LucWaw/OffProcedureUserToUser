plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jib)
}

kotlin { jvmToolchain(21) }

dependencies {
    implementation(project(":stub"))

    runtimeOnly(libs.grpc.netty)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.grpc.testing)
}

tasks.register<JavaExec>("UserToUserServer") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("UserToUserServerKt")
}

val userToUserServerStartScripts =
    tasks.register<CreateStartScripts>("UserToUserServerStartScripts") {
        mainClass.set("UserToUserServerKt")
        applicationName = "hello-world-server"
        outputDir = tasks.named<CreateStartScripts>("startScripts").get().outputDir
        classpath = tasks.named<CreateStartScripts>("startScripts").get().classpath
    }

tasks.named("startScripts") {
    dependsOn(userToUserServerStartScripts)
}

tasks.withType<Test> {
    useJUnit()

    testLogging {
        events =
            setOf(
                org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

jib { container { mainClass = "UserToUserServerKt" } }
