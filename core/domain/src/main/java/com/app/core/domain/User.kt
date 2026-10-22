package com.app.core.domain

data class User(
    val id: String,
    val email: String,
    // Vacío mientras el usuario no cargó su nombre
    val displayName: String = ""
)