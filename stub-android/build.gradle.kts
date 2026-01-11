plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.protobuf)
}

dependencies {
    protobuf(project(":protos"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)

    api(libs.kotlinx.coroutines.core)

    api(libs.grpc.stub)
    api(libs.grpc.protobuf.lite)
    api(libs.grpc.kotlin.stub)
    api(libs.protobuf.kotlin.lite)
}

kotlin { jvmToolchain(21) }

android {
    compileSdk = 36
    buildToolsVersion = "36.0.0"
    namespace = "fr.lucwaw.offprocedureusertouser.stublite"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions { freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn") }
}

protobuf {
    protoc { artifact = libs.protoc.asProvider().get().toString() }
    plugins {
        create("java") { artifact = libs.protoc.gen.grpc.java.get().toString() }
        create("grpc") { artifact = libs.protoc.gen.grpc.java.get().toString() }
        create("grpckt") { artifact = libs.protoc.gen.grpc.kotlin.get().toString() + ":jdk8@jar" }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("java") { option("lite") }
                create("grpc") { option("lite") }
                create("grpckt") { option("lite") }
            }
            it.builtins { create("kotlin") { option("lite") } }
        }
    }
}
