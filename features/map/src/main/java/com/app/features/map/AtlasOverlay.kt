package com.app.features.map

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import android.view.MotionEvent
import androidx.core.graphics.ColorUtils
import com.app.core.domain.RecipeOrigin
import com.app.core.domain.UserLocation
import kotlin.math.hypot
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

/**
 * Capa que dibuja los pines del atlas encima del mapa: un círculo por cada origen (con su
 * etiqueta "Nápoles · 3"), el seleccionado más grande, y la ubicación del usuario en verde.
 * También resuelve el toque sobre un pin. Se dibuja con Canvas porque así el diseño queda
 * igual al mockup, en vez de depender de los marcadores por defecto de la librería.
 */
internal class AtlasOverlay(private val density: Float) : Overlay() {

    var origins: List<RecipeOrigin> = emptyList()
    var selectedKey: String? = null
    var user: UserLocation? = null
    var originColor: Int = Color.RED
    var userColor: Int = Color.GREEN
    var textColor: Int = Color.BLACK
    var labelBackground: Int = Color.WHITE
    var onOriginTapped: (String) -> Unit = {}

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val labelRect = RectF()
    private val scratch = Point()

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val projection = mapView.projection

        user?.let {
            val p = projection.toPixels(GeoPoint(it.latitude, it.longitude), scratch)
            drawUser(canvas, p.x.toFloat(), p.y.toFloat(), it)
        }

        // El seleccionado va último para quedar por encima de los demás.
        origins.sortedBy { it.key == selectedKey }.forEach { origin ->
            val p = projection.toPixels(GeoPoint(origin.latitude, origin.longitude), scratch)
            drawOrigin(canvas, p.x.toFloat(), p.y.toFloat(), origin, origin.key == selectedKey)
        }
    }

    private fun drawOrigin(canvas: Canvas, x: Float, y: Float, origin: RecipeOrigin, selected: Boolean) {
        val radius = (if (selected) 15f else 12f) * density

        if (selected) {
            // Halo translúcido alrededor del origen elegido
            fillPaint.color = ColorUtils.setAlphaComponent(originColor, 0x33)
            canvas.drawCircle(x, y, radius + 8f * density, fillPaint)
        }

        fillPaint.color = originColor
        canvas.drawCircle(x, y, radius, fillPaint)
        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 2.5f * density
        canvas.drawCircle(x, y, radius, strokePaint)

        fillPaint.color = Color.WHITE
        canvas.drawCircle(x, y, 3.5f * density, fillPaint)

        val count = origin.recipes.size
        val name = origin.city.ifBlank { origin.country }
        drawLabel(canvas, x, y + radius + 6f * density, if (count > 1) "$name · $count" else name)
    }

    private fun drawUser(canvas: Canvas, x: Float, y: Float, location: UserLocation) {
        val radius = 9f * density

        fillPaint.color = ColorUtils.setAlphaComponent(userColor, 0x33)
        canvas.drawCircle(x, y, radius + 7f * density, fillPaint)
        fillPaint.color = userColor
        canvas.drawCircle(x, y, radius, fillPaint)
        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 2.5f * density
        canvas.drawCircle(x, y, radius, strokePaint)

        drawLabel(canvas, x, y + radius + 8f * density, location.city.ifBlank { "Vos" })
    }

    /** Etiqueta con fondo claro debajo del pin, para que se lea sobre cualquier parte del mapa. */
    private fun drawLabel(canvas: Canvas, centerX: Float, top: Float, text: String) {
        textPaint.textSize = 11f * density
        val metrics = textPaint.fontMetrics
        val paddingX = 7f * density
        val paddingY = 3f * density
        val width = textPaint.measureText(text)
        val height = metrics.descent - metrics.ascent

        labelRect.set(
            centerX - width / 2 - paddingX,
            top,
            centerX + width / 2 + paddingX,
            top + height + paddingY * 2
        )
        fillPaint.color = labelBackground
        canvas.drawRoundRect(labelRect, 6f * density, 6f * density, fillPaint)

        textPaint.color = textColor
        canvas.drawText(text, centerX, top + paddingY - metrics.ascent, textPaint)
    }

    /** Si el toque cae cerca de un pin, lo selecciona; si no, deja que el mapa lo maneje. */
    override fun onSingleTapConfirmed(event: MotionEvent, mapView: MapView): Boolean {
        val projection = mapView.projection
        var closest: RecipeOrigin? = null
        var closestDistance = 30f * density

        for (origin in origins) {
            val p = projection.toPixels(GeoPoint(origin.latitude, origin.longitude), scratch)
            val distance = hypot(event.x - p.x.toFloat(), event.y - p.y.toFloat())
            if (distance < closestDistance) {
                closest = origin
                closestDistance = distance
            }
        }

        val hit = closest ?: return false
        onOriginTapped(hit.key)
        return true
    }
}