import com.android.tools.build.jetifier.core.utils.Log
import org.jetbrains.kotlin.de.undercouch.gradle.tasks.download.Download

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    // 预下载tflite模型
    id("de.undercouch.download")
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

    // 使用google 的 MLKit 进行文字识别，安装包大小：19.3M, 多15M
//    implementation("com.google.mlkit:text-recognition:16.0.0")

    // 使用tflite 加载模型，安装包大小：9.2M，包含放入asset的模型，多4M
//    implementation("org.tensorflow:tensorflow-lite:0.0.0-nightly")

    // 使用 Tesseract-OCR 方式 ，它在android上的集成,使用Tesseract 4.0版本
//    implementation("cz.adaptech.tesseract4android:tesseract4android:4.7.0")

    // 使用tflite加载官方模型, 添加之前包24.8M

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0")

    implementation("org.tensorflow:tensorflow-lite:0.0.0-nightly-SNAPSHOT")
    implementation("org.tensorflow:tensorflow-lite-gpu:0.0.0-nightly-SNAPSHOT")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:0.0.0-nightly-SNAPSHOT")
    implementation("org.tensorflow:tensorflow-lite-support:0.0.0-nightly-SNAPSHOT")

    implementation("com.quickbirdstudios:opencv:4.5.3.0")
}

val ASSET_DIR = "$projectDir/src/main/assets"

val downloadTextDetectionModelFile by tasks.registering(Download::class) {
    src("https://tfhub.dev/sayakpaul/lite-model/east-text-detector/fp16/1?lite-format=tflite")
    dest(project.file("${ASSET_DIR}/text_detection.tflite"))
    Log.i("GRADLE", "下载 text_detection.tflite")
    overwrite(false)
}

val downloadTextRecognitionModelFile by tasks.registering(Download::class) {
    src("https://tfhub.dev/tulasiram58827/lite-model/keras-ocr/float16/2?lite-format=tflite")
    dest(project.file("${ASSET_DIR}/text_recognition.tflite"))
    Log.i("GRADLE", "下载 text_recognition.tflite")

    overwrite(false)
}

tasks.named("preBuild") {
    dependsOn(downloadTextDetectionModelFile, downloadTextRecognitionModelFile)
}