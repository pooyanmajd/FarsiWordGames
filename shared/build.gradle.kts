import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.room)
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
    ).forEach {
        it.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Core KMP (using bundle)
            implementation(libs.bundles.kmp.core)
            
            // Networking
            implementation(libs.bundles.ktor.common)
            
            // DI - Koin core only per KMP guidelines
            implementation(libs.koin.core)
            
            // Persistence
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            
            // Logging
            implementation(libs.napier)
            
            // Firebase Auth KMP can be added later with platform config
            // implementation(libs.firebase-auth-gitlive)
            
            // TODO: Add KMM Native Coroutines when dependencies are available
            // implementation(libs.kmm.nativecoroutines.core)
            // implementation(libs.kmm.nativecoroutines.annotations)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            implementation(libs.androidx.core.ktx)
            implementation(libs.kotlinx.coroutines.android)
            
            // Koin for Android (platform features)
            implementation(libs.koin.core)
            implementation(libs.koin.android)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            
            // TODO: SwiftUI integration - add when dependencies are available:
            // implementation(libs.koin.swiftui)           // For @KoinViewModel support
            // implementation(libs.kmm.nativecoroutines.core) // For observeState() helper
            
            // For now, iOS will use the shared ViewModel directly:
            // struct WordleView: View {
            //     @StateObject var vm = WordVerificationViewModel() // Direct instantiation
            //     var body: some View {
            //         /* Use vm.uiState directly */
            //     }
            // }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidUnitTest.dependencies {
            implementation(libs.junit)
        }
    }
}

android {
    namespace = "com.pooyan.dev.farsiwords.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}