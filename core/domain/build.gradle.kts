plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    // Solo las anotaciones (@Inject). Sin Hilt: el dominio no conoce el framework de DI.
    api(libs.javax.inject)
    // Flow aparece en los contratos del dominio (AuthRepository.observeCurrentUser)
    api(libs.kotlinx.coroutines.core)
}