package com.app.core.data

import android.util.Log
import com.app.core.domain.AppResult
import com.app.core.domain.DomainError
import com.app.core.domain.Ingredient
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeSearcher
import com.app.core.domain.Visibility
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * Implementación de [RecipeSearcher] con Gemini: a partir de un plato o ingrediente devuelve
 * recetas completas de a [RecipeSearcher.PAGE_SIZE] por página (hasta [RecipeSearcher.MAX_PAGES]
 * páginas). Guarda en memoria lo encontrado para poder abrir el detalle (por id) y para no
 * repetir la llamada si se vuelve a pedir la misma página.
 */
class GeminiRecipeSearcher @Inject constructor(
    @Named("gemini_api_key") private val apiKey: String
) : RecipeSearcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    private val geminiUrl =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent?key=$apiKey"

    private val recipesById = ConcurrentHashMap<String, Recipe>()

    /** Por cada búsqueda (idioma + texto), las páginas ya cargadas, en orden. */
    private val pagesByQuery = ConcurrentHashMap<String, List<List<Recipe>>>()

    override fun find(id: String): Recipe? = recipesById[id]

    override suspend fun search(query: String, languageCode: String, page: Int): AppResult<List<Recipe>> =
        withContext(Dispatchers.IO) {
            val clean = query.trim()
            if (clean.length < MIN_QUERY_LENGTH || page !in 0 until RecipeSearcher.MAX_PAGES) {
                return@withContext AppResult.Success(emptyList())
            }

            val cacheKey = "${languageCode.lowercase()}|${clean.lowercase()}"
            val pages = pagesByQuery[cacheKey].orEmpty()

            // Página ya cargada: se devuelve sin llamar a la IA
            if (page < pages.size) return@withContext AppResult.Success(pages[page])
            // No se puede saltar páginas: la siguiente a pedir es siempre pages.size
            if (page > pages.size) return@withContext AppResult.Success(emptyList())

            try {
                Log.d(TAG, "Buscando '$clean' página ${page + 1} (idioma: $languageCode)")
                val known = pages.flatten()
                val fresh = parseRecipes(callGemini(clean, languageCode, known.map { it.name }))
                    .filter { recipe -> known.none { it.id == recipe.id } }

                fresh.forEach { recipesById[it.id] = it }
                pagesByQuery[cacheKey] = pages + listOf(fresh)
                AppResult.Success(fresh)
            } catch (e: CancellationException) {
                throw e
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Sin internet al buscar", e)
                AppResult.Error(DomainError.Network)
            } catch (e: IOException) {
                Log.e(TAG, "Error de red al buscar", e)
                AppResult.Error(DomainError.Network)
            } catch (e: Exception) {
                Log.e(TAG, "Error al buscar recetas: ${e.message}", e)
                AppResult.Error(DomainError.Unknown(e.message))
            }
        }

    private fun callGemini(query: String, languageCode: String, alreadyShown: List<String>): String {
        val languageName = if (languageCode.equals("en", ignoreCase = true)) "English" else "Spanish"
        val safeQuery = query.replace("\"", "'").take(80)
        val exclusionRule = if (alreadyShown.isEmpty()) {
            ""
        } else {
            "5. NO repitas ninguna de estas recetas, ya fueron mostradas: ${alreadyShown.joinToString(", ")}. Devolvé otras distintas."
        }

        val prompt = """
            Sos un asistente de cocina experto.
            El usuario escribió en un buscador de recetas: "$safeQuery".
            Puede ser un plato (ejemplo: "milanesa"), un ingrediente (ejemplo: "pollo") o ambos.
            Devolvé hasta ${RecipeSearcher.PAGE_SIZE} recetas distintas y reales que coincidan con la búsqueda: si es un plato, ese plato y sus variantes más conocidas; si es un ingrediente, platos populares que lo usen como protagonista.
            Si el texto no tiene relación con la cocina, devolvé una lista vacía de recetas.

            REGLAS:
            1. Cada receta debe tener ingredientes con cantidades estimadas (para una porción estándar) y pasos de preparación completos, paso a paso. NUNCA listas vacías.
            2. Origen: ciudad y país tradicionales del plato. NUNCA dejes el país vacío. Incluí la latitud y longitud aproximadas de esa ciudad como números decimales.
            3. Escribí ABSOLUTAMENTE TODO (nombre, descripción, ingredientes, pasos, ciudad y país) en $languageName.
            4. Devolvé ÚNICAMENTE un JSON válido con esta estructura exacta (sin markdown, sin bloques de código):
            $exclusionRule
            {
              "recetas": [
                {
                  "nombre": "Nombre del plato",
                  "descripcion": "Resumen breve de la receta",
                  "porciones": 4,
                  "tiempo_minutos": 45,
                  "ciudad": "Ciudad de origen",
                  "pais": "País de origen",
                  "latitud": 0.0,
                  "longitud": 0.0,
                  "ingredientes": [
                    {"cantidad": "400 g", "nombre": "Harina"}
                  ],
                  "pasos": [
                    "Paso 1 detallado.",
                    "Paso 2 detallado."
                  ]
                }
              ]
            }
        """.trimIndent()

        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
            })
        }.toString()

        val request = Request.Builder()
            .url(geminiUrl)
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        return client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: throw Exception("Respuesta vacía de Gemini")
            if (response.code == 429) {
                throw IllegalStateException("Se alcanzó el límite de búsquedas con IA. Probá de nuevo en un minuto.")
            }
            if (!response.isSuccessful) {
                Log.e(TAG, "Error Gemini HTTP ${response.code}: $body")
                throw Exception("Error Gemini API: HTTP ${response.code}")
            }
            body
        }
    }

    private fun parseRecipes(rawResponse: String): List<Recipe> {
        val text = JSONObject(rawResponse)
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
            .trim()

        val clean = text.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val array = if (clean.startsWith("[")) {
            JSONArray(clean)
        } else {
            JSONObject(clean).optJSONArray("recetas") ?: JSONArray()
        }

        return (0 until array.length())
            .mapNotNull { i -> array.optJSONObject(i)?.let { toRecipe(it) } }
            .distinctBy { it.id }
    }

    private fun toRecipe(json: JSONObject): Recipe? {
        val name = json.optString("nombre", "").trim()

        val rawIngredients = json.optJSONArray("ingredientes") ?: JSONArray()
        val ingredients = (0 until rawIngredients.length()).mapNotNull { i ->
            val ing = rawIngredients.optJSONObject(i) ?: return@mapNotNull null
            val ingName = ing.optString("nombre", "").trim()
            if (ingName.isEmpty()) null else Ingredient(ing.optString("cantidad", "").trim(), ingName)
        }

        val rawSteps = json.optJSONArray("pasos") ?: JSONArray()
        val steps = (0 until rawSteps.length())
            .map { rawSteps.optString(it, "").trim() }
            .filter { it.isNotEmpty() }

        // Una receta sin nombre, ingredientes o pasos no sirve: se descarta
        if (name.isEmpty() || ingredients.isEmpty() || steps.isEmpty()) return null

        val country = json.optString("pais", "").trim().ifBlank { "Internacional" }

        val lat = json.optDouble("latitud", Double.NaN)
        val lon = json.optDouble("longitud", Double.NaN)
        val validCoords = !lat.isNaN() && !lon.isNaN() &&
                lat in -90.0..90.0 && lon in -180.0..180.0 &&
                !(lat == 0.0 && lon == 0.0)

        return Recipe(
            id = "ai_" + Integer.toHexString("${name.lowercase()}|${country.lowercase()}".hashCode()),
            name = name,
            photoUrl = null,
            city = json.optString("ciudad", "").trim(),
            country = country,
            prepTimeMinutes = json.optInt("tiempo_minutos", 30),
            attempts = 0,
            visibility = Visibility.PUBLIC,
            description = json.optString("descripcion", "").trim(),
            servings = json.optInt("porciones", 4),
            ingredients = ingredients,
            steps = steps,
            latitude = if (validCoords) lat else null,
            longitude = if (validCoords) lon else null
        )
    }

    companion object {
        private const val TAG = "GeminiRecipeSearcher"
        private const val MIN_QUERY_LENGTH = 3
    }
}