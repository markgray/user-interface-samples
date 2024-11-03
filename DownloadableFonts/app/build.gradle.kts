plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.android.downloadablefonts"
        minSdk = 21
        targetSdk = 35
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
        implementation("androidx.activity:activity-ktx:1.9.3")
        implementation("androidx.legacy:legacy-support-v4:1.0.0")
        implementation("androidx.legacy:legacy-support-v13:1.0.0")
        implementation("androidx.cardview:cardview:1.0.0")
        implementation("androidx.appcompat:appcompat:1.7.0")
        implementation("androidx.core:core-ktx:1.15.0")
        implementation("com.google.android.material:material:1.12.0")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    namespace = "com.example.android.downloadablefonts"
}
