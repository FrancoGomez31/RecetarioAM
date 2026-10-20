package com.app.recetario

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Punto de entrada de la app. @HiltAndroidApp le dice a Hilt que genere el
 * contenedor de dependencias (el "grafo") cuando el proceso arranca.
 */
@HiltAndroidApp
class RecetarioApp : Application()