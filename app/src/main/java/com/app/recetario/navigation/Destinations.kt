package com.app.recetario.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Cada pantalla es una "key" tipada. El back stack es literalmente una lista de estas keys,
// por ejemplo [RecipesDestination, AccountDestination] cuando abrís Mi cuenta.
// @Serializable permite guardar esa lista al rotar la pantalla o si el sistema mata el proceso.

@Serializable
data object RecipesDestination : NavKey

@Serializable
data object MapDestination : NavKey

@Serializable
data object ExploreDestination : NavKey

@Serializable
data object AchievementsDestination : NavKey

// Pantallas que se abren encima de una pestaña (ahí la barra inferior se oculta sola)
@Serializable
data object AccountDestination : NavKey