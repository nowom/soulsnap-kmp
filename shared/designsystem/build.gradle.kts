plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        namespace = "pl.soulsnaps.designsystem"
        compileSdk = 36
minSdk = 26

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

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
                api(compose.foundation)
                api(compose.material3)
                api(compose.ui)
                implementation(libs.material.icons.extended)
                implementation(compose.components.resources)
                api(compose.components.resources)
                implementation(libs.kotlinx.datetime)
                implementation(libs.koin.core)
                // Add KMP dependencies here
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            resources.srcDirs("src/androidMain/res")
            dependencies {
                implementation("androidx.activity:activity-compose:1.7.2")
                implementation(libs.kotlinx.datetime)
                implementation(libs.androidx.camera.core)
                implementation(libs.androidx.camera.view)
                implementation(libs.androidx.camera.lifecycle)
                implementation(libs.androidx.camera.camera2)
                implementation("com.google.accompanist:accompanist-permissions:0.37.3")
                implementation("com.google.android.gms:play-services-location:21.0.1")
                implementation("com.google.android.gms:play-services-maps:18.2.0")
                implementation("androidx.media:media:1.7.0")
                implementation("androidx.media3:media3-exoplayer:1.2.1")
                implementation("androidx.media3:media3-ui:1.2.1")
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.testExt.junit)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.kotlinx.datetime)
            }
        }
    }

}