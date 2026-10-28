package com.app.core.domain

data class Attempt(
    val id: String,
    val recipeId: String,
    val number: Int,
    val createdAtMillis: Long,
    val note: String = "",
    val photos: List<AttemptPhoto> = emptyList()
)

data class AttemptPhoto(
    val id: String,
    val stepIndex: Int,
    val uri: String,
    val takenAtMillis: Long
)

data class NewAttemptPhoto(
    val stepIndex: Int,
    val sourceUri: String
)
