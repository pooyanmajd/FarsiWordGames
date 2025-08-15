import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.room)
    alias(libs.plugins.kmm.nativecoroutines)
}

kotlin {
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.napier)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kmm.nativecoroutines.core)         // <-- here
                implementation(libs.kmm.nativecoroutines.annotations)   // <-- here
            }
        }

        val androidMain by getting {
            dependencies { implementation(libs.koin.android) }
        }

        // keep iosMain if you truly need iOS-only deps, but not required for KMP-NC
        val iosMain by creating { dependsOn(commonMain) }
        val iosX64Main by getting; val iosArm64Main by getting; val iosSimulatorArm64Main by getting
        listOf(iosX64Main, iosArm64Main, iosSimulatorArm64Main).forEach { it.dependsOn(iosMain) }
    }
}

/*kotlin {
    // ... your androidTarget() and listOf(iosX64(), ...) definitions are correct ...

    androidTarget()

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

        // 1. CREATE the intermediate iosMain source set
        val iosMain by creating {
            // 2. Make it depend on commonMain to inherit its code/dependencies
            dependsOn(commonMain)
            dependencies {
                implementation(libs.kmm.nativecoroutines.core)
                // Add other iOS-specific dependencies here
            }
        }

        // 3. Make the actual iOS target source sets depend on the new iosMain
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        listOf(iosX64Main, iosArm64Main, iosSimulatorArm64Main).forEach {
            it.dependsOn(iosMain)
        }

        // You can follow a similar pattern for test source sets if needed
        // val commonTest by getting { ... }
        // val iosTest by creating { dependsOn(commonTest) }
        // ...
    }
}*/


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