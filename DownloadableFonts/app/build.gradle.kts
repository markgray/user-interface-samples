import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.android.downloadablefonts"
        minSdk = 21
        targetSdk = 36
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
    dependencies {
        implementation("androidx.activity:activity-ktx:1.10.1")
        implementation("androidx.legacy:legacy-support-v4:1.0.0")
        implementation("androidx.legacy:legacy-support-v13:1.0.0")
        implementation("androidx.cardview:cardview:1.0.0")
        implementation("androidx.appcompat:appcompat:1.7.1")
        implementation("androidx.core:core-ktx:1.16.0")
        implementation("com.google.android.material:material:1.12.0")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }
    namespace = "com.example.android.downloadablefonts"
}
