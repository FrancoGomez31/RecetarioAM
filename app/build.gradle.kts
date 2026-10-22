plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // Genera la serialización de las rutas de navegación (@Serializable)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    // Procesa app/google-services.json (solo va en el módulo :app)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.app.recetario"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.app.recetario"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Módulos del proyecto
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:firebase"))
    implementation(project(":core:ui"))
    implementation(project(":features:auth"))
    implementation(project(":features:recipes"))
    implementation(project(":features:account"))
    implementation(project(":features:map"))

    // Compose y Android
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)

    // Lifecycle y ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation 3: el back stack es una lista que manejamos nosotros
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    // Un ViewModelStore por pantalla del back stack
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.core)

    // Hilt: el grafo de dependencias de toda la app se ensambla acá
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // hiltViewModel(): el puente entre el grafo de Hilt y los ViewModel de Compose
    implementation(libs.androidx.hilt.navigation.compose)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}