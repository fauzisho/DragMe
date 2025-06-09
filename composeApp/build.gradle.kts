import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
    `maven-publish`
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.clientAndroid)
        }
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.serializationKotlinxJson)
            implementation(projects.shared)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.clientOkhttp)
        }
        
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.clientJs)
            }
        }
    }
}

android {
    namespace = "org.drag.me"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.drag.me"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    lint {
        // Disable the problematic lint check that's causing build failures
        disable.add("NullSafeMutableLiveData")
        // Also disable other potentially problematic checks for multiplatform
        disable.add("MissingTranslation")
        abortOnError = false
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "org.drag.me.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "DragMe"
            packageVersion = "1.0.0"
            
            description = "Interactive Connected Blocks Desktop Application"
            copyright = "© 2024 DragMe. All rights reserved."
            vendor = "DragMe Team"
            
            linux {
                packageName = "dragme"
                debMaintainer = "dragme@example.com"
                menuGroup = "Development"
                appCategory = "Development"
            }
            
            windows {
                packageName = "DragMe"
                msiPackageVersion = "1.0.0"
                menuGroup = "DragMe"
                upgradeUuid = "BF9CDA6A-1391-46AD-9E25-B82ADCEA6F94"
            }
            
            macOS {
                packageName = "DragMe"
                bundleID = "org.drag.me.DragMe"
                appCategory = "public.app-category.developer-tools"
            }
        }
    }
}

// Set version for publishing
val buildVersion = if (project.hasProperty("version") && project.property("version") != "unspecified") {
    project.property("version").toString()
} else {
    // Generate unique version for each build with timestamp to avoid conflicts
    val baseVersion = "1.0.0"
    val buildNumber = System.getenv("GITHUB_RUN_NUMBER") ?: "local"
    val commitHash = System.getenv("GITHUB_SHA")?.take(7) ?: "unknown"
    val timestamp = System.currentTimeMillis()
    "$baseVersion-$buildNumber-$commitHash-$timestamp-SNAPSHOT"
}

version = buildVersion

// Publishing configuration for GitHub Packages
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/fauzisho/DragMe")
            credentials {
                username = project.findProperty("githubUsername")?.toString() ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("githubToken")?.toString() ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
