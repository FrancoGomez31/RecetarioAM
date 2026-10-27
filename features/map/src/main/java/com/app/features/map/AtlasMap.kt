package com.app.features.map

import android.view.MotionEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.app.core.domain.RecipeOrigin
import com.app.core.domain.UserLocation
import com.app.core.ui.LeafGreen
import java.io.File
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView

private const val DEFAULT_SINGLE_POINT_ZOOM = 5.0

/**
 * El mapamundi del atlas: un MapView de osmdroid con la capa de pines encima, más los
 * botones de zoom y de centrar en la ubicación del usuario.
 */
@Composable
internal fun AtlasMap(
    origins: List<RecipeOrigin>,
    selectedKey: String?,
    user: UserLocation?,
    fit: FitRequest?,
    onOriginTapped: (String) -> Unit,
    onLocate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = context.resources.displayMetrics.density
    // El mapa y su capa viven más que una recomposición: este State apunta siempre al callback más nuevo.
    val currentOnOriginTapped by rememberUpdatedState(onOriginTapped)

    // also { it... } y no apply { ... }: dentro de apply, "onOriginTapped" se confundiría con el
    // parámetro de esta función (que tiene el mismo nombre) en vez de la propiedad del overlay.
    val overlay = remember {
        AtlasOverlay(density).also { atlasOverlay ->
            atlasOverlay.onOriginTapped = { key -> currentOnOriginTapped(key) }
        }
    }

    val mapView = remember {
        // Mismo criterio que el selector: User-Agent propio y caché en almacenamiento interno.
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles")
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            minZoomLevel = 2.0
            maxZoomLevel = 18.0
            // Sin repetir el mundo a los costados
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false
            controller.setZoom(2.5)
            controller.setCenter(GeoPoint(20.0, 0.0))
            overlays.add(overlay)

            // El mapa vive dentro de una pantalla con scroll vertical: mientras el dedo está
            // sobre el mapa, se le pide al padre que no robe el gesto (si no, arrastrar sería imposible).
            setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN ->
                        view.parent?.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                        view.parent?.requestDisallowInterceptTouchEvent(false)
                }
                // false: el MapView igual procesa el gesto
                false
            }
        }
    }

    val originColor = MaterialTheme.colorScheme.primary.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val labelBackground = MaterialTheme.colorScheme.surface.toArgb()
    val userColor = LeafGreen.toArgb()

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onDetach()
        }
    }

    // Cada vez que llega un pedido nuevo (o se vuelve a crear el mapa), se ejecuta el encuadre.
    LaunchedEffect(fit?.token) {
        if (fit != null) applyFit(mapView, fit, paddingPx = (48 * density).toInt())
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                overlay.origins = origins
                overlay.selectedKey = selectedKey
                overlay.user = user
                overlay.originColor = originColor
                overlay.userColor = userColor
                overlay.textColor = textColor
                overlay.labelBackground = labelBackground
                view.invalidate()
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MapControlButton(Icons.Filled.Add, "Acercar") { mapView.controller.zoomIn() }
            MapControlButton(Icons.Filled.Remove, "Alejar") { mapView.controller.zoomOut() }
            MapControlButton(Icons.Filled.MyLocation, "Mi ubicación", onClick = onLocate)
        }

        // La licencia de OpenStreetMap exige mostrar esta atribución sobre el mapa.
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ) {
            Text(
                text = "© colaboradores de OpenStreetMap",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun MapControlButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Ejecuta un pedido: encuadra varios puntos, o centra uno solo. */
private fun applyFit(mapView: MapView, request: FitRequest, paddingPx: Int) {
    val points = request.points.map { GeoPoint(it.latitude, it.longitude) }
    if (points.isEmpty()) return

    if (points.size == 1) {
        val point = points.first()
        when {
            // Todavía sin medidas (primer dibujado): no se puede animar, se posiciona directo.
            mapView.width == 0 -> {
                mapView.controller.setZoom(request.zoom ?: DEFAULT_SINGLE_POINT_ZOOM)
                mapView.controller.setCenter(point)
            }
            request.keepZoom -> mapView.controller.animateTo(point)
            else -> mapView.controller.animateTo(point, request.zoom ?: DEFAULT_SINGLE_POINT_ZOOM, 700L)
        }
        return
    }

    val box = BoundingBox.fromGeoPoints(points)
    if (mapView.width > 0) {
        mapView.zoomToBoundingBox(box, true, paddingPx)
    } else {
        // El mapa todavía no se midió: se espera al primer layout para encuadrar.
        mapView.addOnFirstLayoutListener { _, _, _, _, _ ->
            mapView.zoomToBoundingBox(box, false, paddingPx)
        }
    }
}