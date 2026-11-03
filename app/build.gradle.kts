import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android.plugin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.mechat.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mechat.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        // ICE servers from local.properties / CI secrets — NEVER hard-coded (RULE 4).
        // e.g. in local.properties: MECHTURN_URL=turns:relay.example.org:443, MECHTURN_USER=..., MECHTURN_CRED=...
        val props = Properties().apply {
            val f = rootProject.file("local.properties")
            if (f.exists()) f.inputStream().use { load(it) }
        }
        fun prop(name: String) = (props.getProperty(name) ?: "").replace("\"", "\\\"")
        buildConfigField("String", "STUN_URL", "\"${prop("MECHAT_STUN_URL")}\"")
        buildConfigField("String", "TURN_URL", "\"${prop("MECHTURN_URL")}\"")
        buildConfigField("String", "TURN_USER", "\"${prop("MECHTURN_USER")}\"")
        buildConfigField("String", "TURN_CRED", "\"${prop("MECHTURN_CRED")}\"")
    }

    // Release signing: reads from environment variables or ~/.gradle/gradle.properties
    // (MECHAT_KEYSTORE, MECHAT_KEYSTORE_PASSWORD, MECHAT_KEY_ALIAS, MECHAT_KEY_PASSWORD).
    // NEVER commit the keystore or passwords (RULE 4).
    signingConfigs {
        create("release") {
            val props = Properties().apply {
                val f = gradle.gradleUserHomeDir.resolve("gradle.properties")
                if (f.exists()) f.inputStream().use { load(it) }
            }
            fun setting(name: String): String? =
                System.getenv(name) ?: props.getProperty(name)
            val ksPath = setting("MECHAT_KEYSTORE")
            if (ksPath != null) {
                storeFile = file(ksPath)
                storePassword = setting("MECHAT_KEYSTORE_PASSWORD")
                keyAlias = setting("MECHAT_KEY_ALIAS")
                keyPassword = setting("MECHAT_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Crypto: libsodium (RULE 3 — never hand-rolled crypto)
    implementation(libs.lazysodium.android)
    implementation(libs.datastore.preferences)
    implementation("androidx.lifecycle:lifecycle-process:${libs.versions.lifecycleProcess.get()}")

    // Local storage: Room over SQLCipher (encrypted at rest, raw 32-byte key — no weak KDF)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.sqlcipher)
    implementation(libs.sqlite.ktx)
    ksp(libs.room.compiler)

    // QR: ZXing core (ML Kit-free) + CameraX for scanning
    implementation(libs.zxing.core)
    implementation(libs.camerax.core)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // WebRTC: official org.webrtc library
    implementation(libs.webrtc)

    // Disappearing messages sweep
    implementation(libs.workmanager)

    // Attachments: EXIF stripping
    implementation(libs.exifinterface)

    // Network: OkHttp WebSocket (wss:// only) + typed protocol
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation(libs.lazysodium.java)
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
