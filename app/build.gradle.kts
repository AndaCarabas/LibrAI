plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id ("kotlin-parcelize")
    kotlin("plugin.serialization") version "1.9.10"
}

android {
    namespace = "com.example.librai"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.librai"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        val geminiKey: String = project
            .findProperty("GEMINI_API_KEY") as? String
            ?: throw GradleException(
                "Please define GEMINI_API_KEY in your gradle.properties"
            )

        buildConfigField(
            type  = "String",
            name  = "GEMINI_API_KEY",
            value = "\"$geminiKey\""
        )

        val openAiKey: String = project
            .findProperty("OPENAI_API_KEY") as? String
            ?: throw GradleException(
                "Please define OPENAI_API_KEY in your gradle.properties"
            )

        buildConfigField(
            type  = "String",
            name  = "OPENAI_API_KEY",
            value = "\"$openAiKey\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true

    }
}

dependencies {

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-firestore")

    // ML Kit Barcode Scanner
    implementation(libs.barcode.scanning)

    // CameraX
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.gson)

    implementation(libs.guava)
    implementation(libs.coil.compose)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.permissions)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    //implementation(libs.androidx.material3)
    implementation("androidx.compose.material3:material3:1.4.0-alpha15")
    implementation(libs.firebase.storage.ktx)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.firebase.common.ktx)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.core.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.datastore.preferences)

}