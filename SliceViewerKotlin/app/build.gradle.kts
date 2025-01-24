plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.10.0")
    implementation("androidx.slice:slice-view:1.1.0-alpha02")
    implementation("androidx.slice:slice-core:1.0.0")
    implementation("androidx.slice:slice-builders:1.0.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-common:2.8.7")
    //noinspection GradleDependency TODO: figure out why the newer versions use `val` for header
    implementation("androidx.slice:slice-builders-ktx:1.0.0-alpha3")
}

android {
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.android.sliceviewer"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    namespace = "com.example.android.sliceviewer"
}
