plugins {
    alias(libs.plugins.android.library)
    // Genera la deserialización de las respuestas JSON de Nominatim (@Serializable)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.app.core.data"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Módulos del proyecto
    implementation(project(":core:domain"))
    implementation(project(":core:firebase"))

    implementation(libs.androidx.core.ktx)
    // Dispatchers.IO: copiar archivos (fotos) fuera del hilo principal
    implementation(libs.kotlinx.coroutines.core)

    // Red: Retrofit convierte una interfaz Kotlin en llamadas HTTP, OkHttp las ejecuta
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)

    // Ubicación del dispositivo (FusedLocationProviderClient)
    implementation(libs.play.services.location)

    // Hilt: KSP genera el código de inyección de este módulo
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}