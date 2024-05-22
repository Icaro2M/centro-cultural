plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)

    id("com.google.gms.google-services")


}

android {
    namespace = "com.example.centrocultural"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.centrocultural"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("com.google.android.material:material: 1.2.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("com.journeyapps:zxing-android-embedded:3.0.1@aar")
    implementation ("com.google.zxing:core:3.2.0")
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation(platform("com.google.firebase:firebase-firestore-ktx:25.0.0"))
    implementation(libs.firebase.common.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation("de.hdodenhof:circleimageview:3.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


}