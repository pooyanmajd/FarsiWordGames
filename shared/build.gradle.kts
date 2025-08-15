import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.room)
}

kotlin {
    androidTarget()
    // iOS targets if any...

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.napier)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                // Add others like serialization if used
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.koin.android)
                // Android-specific
            }
        }

        // commonTest, etc.
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