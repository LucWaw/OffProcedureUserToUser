plugins {
    application
    alias(libs.plugins.kotlin.jvm)
}

kotlin { jvmToolchain(21) }

dependencies {
    implementation(project(":stub"))
    runtimeOnly(libs.grpc.netty)
    implementation(libs.kotlinx.coroutines.core)
}

tasks.register<JavaExec>("UtoUClient") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("UtoUClientKt")
}

val helloWorldClientStartScripts =
    tasks.register<CreateStartScripts>("UtoUClientStartScripts") {
        mainClass.set("UtoUClientKt")
        applicationName = "utou-kotlin-client"
        outputDir = tasks.named<CreateStartScripts>("startScripts").get().outputDir
        classpath = tasks.named<CreateStartScripts>("startScripts").get().classpath
    }


tasks.named("startScripts") {
    dependsOn(helloWorldClientStartScripts)
}