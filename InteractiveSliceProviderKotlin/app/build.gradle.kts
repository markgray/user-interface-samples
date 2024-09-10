plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.android.interactivesliceprovider"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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
    namespace = "com.example.android.interactivesliceprovider"
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.13.1")

    // The slice builder ktx library has a number of dependencies. For reference, since this is a
    // slice sample, below are a list of the slice dependencies:
    // implementation "androidx.slice:slice-core:latest-version"
    // implementation "androidx.slice:slice-builders:latest-version"
    implementation("androidx.slice:slice-core:1.0.0")
    implementation("androidx.slice:slice-builders:1.0.0")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-appindexing:20.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    //noinspection GradleDependency TODO: figure out why the newer versions use `val` for header
    implementation("androidx.slice:slice-builders-ktx:1.0.0-alpha3")
}
