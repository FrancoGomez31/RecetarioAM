// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    // Se declaran acá con `apply false` y cada módulo aplica solo los que necesita.
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    // Lee google-services.json y genera la configuración de Firebase
    alias(libs.plugins.google.services) apply false
}