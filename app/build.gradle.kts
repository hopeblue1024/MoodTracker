plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.moodtracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.moodtracker"
        minSdk = 29       // Android 10 及以上
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
            // ═══════════════════════════════════════════════════════════════
            // 默认使用 debug 签名，方便快速打出可安装测试 APK 用于 Release
            //
            // 如需正式签名，请在 GitHub 仓库 Settings → Secrets 中配置:
            //   SIGNING_KEYSTORE     — base64 编码的 keystore 文件内容
            //   SIGNING_KEY_ALIAS    — 密钥别名
            //   SIGNING_KEY_PASSWORD — 密钥密码
            //   SIGNING_STORE_PASSWORD — keystore 密码
            //
            // 然后在 CI 中解码 keystore 并配置 signingConfigs.release
            // ═══════════════════════════════════════════════════════════════
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // ── Compose BOM ──
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // ── Navigation ──
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // ── Lifecycle ──
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-process:2.8.6")

    // ── Room (本地 SQLite 数据库) ──
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ── 生物识别 (指纹解锁) ──
    implementation("androidx.biometric:biometric:1.1.0")

    // ── Coroutines ──
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ── Core & Activity ──
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
}
