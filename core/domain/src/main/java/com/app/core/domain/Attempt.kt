package com.app.core.domain

/** Una vez que el usuario cocina la receta: queda su número, la fecha, una nota y las fotos del proceso. */
data class Attempt(
    /** Vacío = intento nuevo todavía sin guardar; quien lo guarda le asigna el id. */
    val id: String,
    val recipeId: String,
    /** 1 es el primer intento. Se muestra como "Intento 3". */
    val number: Int,
    val createdAtMillis: Long,
    val note: String = "",
    val photos: List<AttemptPhoto> = emptyList()
)

/** Foto de un intento, asociada al paso de la receta en el que se sacó. */
data class AttemptPhoto(
    val id: String,
    /** Índice del paso en Recipe.steps (0 = Paso 1). */
    val stepIndex: Int,
    /** Referencia permanente a la imagen. Hoy es un archivo local; después será una URL de la nube. */
    val uri: String,
    val takenAtMillis: Long
)

/** Foto recién elegida (cámara o galería) que todavía no se guardó. [sourceUri] es temporal. */
data class NewAttemptPhoto(
    val stepIndex: Int,
    val sourceUri: String
)