package com.app.core.domain

data class Recipe(
    val id: String,
    val name: String,
    val photoUrl: String?,
    val city: String,
    val country: String,
    val prepTimeMinutes: Int,
    val attempts: Int,
    val visibility: Visibility
)