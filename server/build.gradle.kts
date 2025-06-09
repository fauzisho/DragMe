plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.drag.me"
version = "1.0.0"

application {
    mainClass.set("org.drag.me.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf(
        "-Dio.ktor.development=$isDevelopment",
        "-Xmx256m", // Limit max memory to 256MB
        "-Xms128m", // Start with 128MB
        "-XX:+UseG1GC", // Use efficient garbage collector
        "-XX:MaxGCPauseMillis=100" // Limit GC pause time
    )
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serverCors)
    implementation(libs.ktor.serializationKotlinxJson)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}

// Configure shadow plugin to create fat JAR
tasks.shadowJar {
    archiveFileName.set("server-all.jar")
    mergeServiceFiles()
}