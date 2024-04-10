plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "cn.ltt.luck.numberidentify"
    compileSdk = 34

    defaultConfig {
        applicationId = "cn.ltt.luck.numberidentify"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")

    implementation("com.github.hoc081098:ViewBindingDelegate:1.4.0")
    // 只加载了如上jar包，安装包大小：5.4M

    // 使用google 的 MLKit 进行文字识别，安装包大小：19.3M
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // 使用tflite 加载模型，安装包大小：9.2M，包含放入asset的模型
    implementation("org.tensorflow:tensorflow-lite:0.0.0-nightly")
}