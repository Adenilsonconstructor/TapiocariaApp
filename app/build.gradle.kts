plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.adenilsonsilva.tapiocariaapp"
    compileSdk = 34 // A sua versão pode ser diferente

    defaultConfig {
        applicationId = "com.adenilsonsilva.tapiocariaapp"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Armazenamento de dados Firebase
    implementation("com.google.firebase:firebase-storage")
    // Banco de dados Firebase
    implementation("com.google.firebase:firebase-firestore")
    // Autenticaação com Firebase
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("androidx.core:core-ktx:1.12.0") // A sua versão pode ser diferente
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // (NOVO) Dependência para a Splash Screen API
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Dependência para usar a delegação 'by viewModels()'
    implementation("androidx.activity:activity-ktx:1.8.2")

    // Dependências do Room Database
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // Dependência do LiveData KTX para usar o 'asLiveData'
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}