import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    // TODO: Add kmm-nativecoroutines plugin when repository is configured
    // alias(libs.plugins.kmm.nativecoroutines) // For SwiftUI observeState() helper
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
            
            // DI (keeping existing + adding improvements)
            implementation(libs.koin.core)
            implementation(libs.koin.annotations) // For @Singleton, @Factory etc
            
            // Persistence
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            
            // Logging
            implementation(libs.napier)
            
            // TODO: Add KMM Native Coroutines when dependencies are available
            // implementation(libs.kmm.nativecoroutines.core)
            // implementation(libs.kmm.nativecoroutines.annotations)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            implementation(libs.androidx.core.ktx)
            implementation(libs.kotlinx.coroutines.android)
            
            // Enhanced Koin for Android
            implementation(libs.bundles.koin.android.bundle)
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

dependencies {
    add("kspCommonMainMetadata", libs.room.compiler)
    // TODO: Add KSP for Koin annotations later when properly configured
    // add("kspCommonMainMetadata", libs.koin.annotations)
    // add("kspAndroid", libs.koin.annotations)
    // add("kspIosX64", libs.koin.annotations)
    // add("kspIosArm64", libs.koin.annotations)
    // add("kspIosSimulatorArm64", libs.koin.annotations)
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}