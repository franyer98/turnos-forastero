plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.franyer.turnosforastero"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.franyer.turnosforastero"
        minSdk = 24
        targetSdk = 34
        // Cada compilación en GitHub Actions sube la versión sola (la app la usa para auto-actualizarse)
        val corrida = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 0
        versionCode = 100 + corrida
        versionName = "1.$corrida"
    }

    signingConfigs {
        create("personal") {
            storeFile = file("firma.jks")
            storePassword = "turnos123"
            keyAlias = "turnos"
            keyPassword = "turnos123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("personal")
        }
        debug {
            signingConfig = signingConfigs.getByName("personal")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
