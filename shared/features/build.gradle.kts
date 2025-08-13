plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.1.21" // dopasuj do wersji Kotlin
}

kotlin {
    androidTarget()

    sourceSets {
        androidMain {
            dependencies {
                // Android dependencies here
            }
        }
    }
    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets


    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
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

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(compose.runtime)
                implementation(libs.lifecycle.viewmodel.compose)
                implementation(libs.koin.core)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.coil.compose)
                implementation(libs.navigation.compose)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
                implementation(libs.material.icons.extended)
                implementation(project(":shared:designsystem"))
                implementation(project(":shared:database"))
                implementation(libs.sqldelight.coroutines)
                implementation(libs.kotlinx.datetime)

                // Add KMP dependencies here
                // Ktor (KMP)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                


            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            sourceSets {
                resources.srcDirs("src/androidMain/res")
            }
            dependencies {
                implementation(libs.mapbox.compose.android)
                implementation(libs.mapbox.core.android)
                implementation(libs.androidx.datastore)
                implementation(libs.ktor.client.android)
            }
        }



        iosMain {
            dependencies {
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
                implementation(libs.ktor.client.darwin)
            }
        }
    }

}

android {
    namespace = "pl.soulsnaps.features"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
}