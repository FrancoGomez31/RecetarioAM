package com.app.core.ui

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

private val ArgentineLocale: Locale = Locale.forLanguageTag("es-AR")

/** Redondea para que no parezca más preciso de lo que es: "≈ 11.200 km". */
fun formatDistance(km: Double): String {
    if (km < 10) return "< 10 km"
    val rounded = if (km >= 1000) (km / 100).roundToInt() * 100 else (km / 10).roundToInt() * 10
    return "≈ ${NumberFormat.getIntegerInstance(ArgentineLocale).format(rounded)} km"
}