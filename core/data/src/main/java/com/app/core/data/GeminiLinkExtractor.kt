package com.app.core.data

import android.util.Log
import com.app.core.domain.AppResult
import com.app.core.domain.DomainError
import com.app.core.domain.Ingredient
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeLinkExtractor
import com.app.core.domain.Visibility
import java.io.IOException
import java.net.UnknownHostException
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
 * Implementación de [RecipeLinkExtractor]:
 *  - Extrae título y descripción completa del video (o página web).
 *  - En YouTube: busca `shortDescription` en el HTML de YouTube, donde los creadores suelen poner la receta completa.
 *  - Envía la información a Gemini (modelo `gemini-3.5-flash-lite`) con un prompt inteligente que reconstruye e infiere
 *    los ingredientes y pasos completos aún cuando la descripción sea resumida.
 *  - Devuelve una [Recipe] borrador completa y detallada.
 */
class GeminiLinkExtractor @Inject constructor(
    @Named("gemini_api_key") private val apiKey: String
) : RecipeLinkExtractor {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val geminiUrl =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent?key=$apiKey"

    override suspend fun extract(url: String, languageCode: String): AppResult<Recipe> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando extracción para link: $url (idioma: $languageCode)")
            val pageInfo = fetchPageInfo(url)
            val recipe = callGemini(pageInfo, languageCode)
            AppResult.Success(recipe)
        } catch (e: CancellationException) {
            throw e
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Sin internet al extraer link", e)
            AppResult.Error(DomainError.Network)
        } catch (e: IOException) {
            Log.e(TAG, "Error de red al extraer link", e)
            AppResult.Error(DomainError.Network)
        } catch (e: Exception) {
            Log.e(TAG, "Error al extraer receta: ${e.message}", e)
            AppResult.Error(DomainError.Unknown(e.message))
        }
    }

    private data class PageInfo(val title: String, val description: String)

    private fun fetchPageInfo(url: String): PageInfo {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
            .build()

        val html = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code} al acceder al link")
            response.body?.string() ?: throw Exception("Respuesta vacía del link")
        }

        val isYouTube = url.contains("youtube.com", ignoreCase = true) || url.contains("youtu.be", ignoreCase = true)

        val title = if (isYouTube) {
            extractYouTubeTitle(html)
        } else {
            extractMetaProperty(html, "og:title")
                ?: extractRegex(html, """<title[^>]*>([^<]+)</title>""")
                ?: ""
        }

        val description = if (isYouTube) {
            extractYouTubeFullDescription(html)
        } else {
            extractMetaProperty(html, "og:description")
                ?: extractMetaName(html, "description")
                ?: ""
        }

        val cleanTitle = title.replace(" - YouTube", "").trim()
        val cleanDesc = unescapeJsonString(description).trim()

        if (cleanTitle.isBlank() && cleanDesc.isBlank()) {
            throw Exception("No se pudo obtener información del link")
        }

        Log.d(TAG, "Info extraída -> Título: '$cleanTitle', Longitud desc: ${cleanDesc.length}")
        return PageInfo(title = cleanTitle, description = cleanDesc)
    }

    private fun extractYouTubeTitle(html: String): String {
        return extractRegex(html, """"title":"(.*?)"(?=,"lengthSeconds")""")
            ?: extractRegex(html, """<title[^>]*>([^<]+)</title>""")
            ?: extractMetaProperty(html, "og:title")
            ?: ""
    }

    private fun extractYouTubeFullDescription(html: String): String {
        // La descripción completa de YouTube se encuentra en shortDescription dentro de los datos JSON embebidos
        return extractRegex(html, """"shortDescription":"(.*?)"(?=,"isCrawlable")""")
            ?: extractRegex(html, """"description":\{"simpleText":"(.*?)"\}""")
            ?: extractMetaProperty(html, "og:description")
            ?: ""
    }

    private fun extractMetaProperty(html: String, property: String): String? {
        val regex = Regex("""<meta[^>]+property=["']${Regex.escape(property)}["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        return regex.find(html)?.groupValues?.getOrNull(1)
    }

    private fun extractMetaName(html: String, name: String): String? {
        val regex = Regex("""<meta[^>]+name=["']${Regex.escape(name)}["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        return regex.find(html)?.groupValues?.getOrNull(1)
    }

    private fun extractRegex(text: String, pattern: String): String? {
        val regex = Regex(pattern, RegexOption.DOT_MATCHES_ALL)
        return regex.find(text)?.groupValues?.getOrNull(1)
    }

    private fun unescapeJsonString(input: String): String {
        return try {
            JSONObject("{\"v\":\"$input\"}").optString("v", input)
        } catch (_: Exception) {
            input.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\")
        }
    }

    private fun callGemini(pageInfo: PageInfo, languageCode: String): Recipe {
        val targetLanguageName = if (languageCode.equals("en", ignoreCase = true)) "English" else "Spanish"
        val prompt = """
            Sos un asistente de cocina experto.
            A partir del título y la descripción de un video o publicación de cocina, generá la receta completa y detallada del plato mencionado.

            REGLAS FUNDAMENTALES:
            1. Si la descripción contiene la receta (ingredientes y pasos), extraela fielmente y ordenala.
            2. Si la descripción es breve o no lista ingredientes paso a paso (ejemplo: solo saluda o menciona el plato), vos CONOCES la receta tradicional del plato ('${pageInfo.title}'). DEBES inferir y reconstruir la lista completa de ingredientes típicos con sus cantidades estimadas (para un tamaño estándar) y los pasos de preparación paso a paso.
            3. NUNCA devuelvas listas de ingredientes o pasos vacías. Siempre completá la receta para que el usuario tenga una base útil para cocinar.
            4. IDIOMA OBLIGATORIO DE SALIDA: Debes escribir ABSOLUTAMENTE TODO el resultado (nombre del plato, cantidades y nombres de ingredientes, y cada paso de preparación) obligatoriamente en $targetLanguageName.
            5. Devolvé ÚNICAMENTE un JSON válido con esta estructura exacta (sin formato markdown, sin bloques de código ```json):
            {
              "nombre": "Nombre del plato",
              "porciones": 8,
              "tiempo_minutos": 45,
              "ingredientes": [
                {"cantidad": "400 g", "nombre": "Harina"}
              ],
              "pasos": [
                "Paso 1 detallado.",
                "Paso 2 detallado."
              ]
            }

            Datos de la publicación:
            Título: ${pageInfo.title}
            Descripción: ${pageInfo.description.take(4000)}
        """.trimIndent()

        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
        }.toString()

        val request = Request.Builder()
            .url(geminiUrl)
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val responseText = client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: throw Exception("Respuesta vacía de Gemini")
            if (!response.isSuccessful) {
                Log.e(TAG, "Error Gemini HTTP ${response.code}: $body")
                throw Exception("Error Gemini API: HTTP ${response.code}")
            }
            body
        }

        return parseGeminiResponse(responseText)
    }

    private fun parseGeminiResponse(rawResponse: String): Recipe {
        val root = JSONObject(rawResponse)
        val text = root
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
            .trim()

        val clean = text
            .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

        val json = JSONObject(clean)
        val ingredientes = json.optJSONArray("ingredientes") ?: JSONArray()
        val pasos = json.optJSONArray("pasos") ?: JSONArray()

        return Recipe(
            id = "",
            name = json.optString("nombre", "").ifBlank { "Receta importada" },
            photoUrl = null,
            city = "", country = "",
            prepTimeMinutes = json.optInt("tiempo_minutos", 30),
            attempts = 0,
            visibility = Visibility.PRIVATE,
            servings = json.optInt("porciones", 4),
            ingredients = (0 until ingredientes.length()).map { i ->
                val ing = ingredientes.getJSONObject(i)
                Ingredient(quantity = ing.optString("cantidad", ""), name = ing.optString("nombre", ""))
            },
            steps = (0 until pasos.length()).map { i -> pasos.getString(i) }
        )
    }

    companion object {
        private const val TAG = "GeminiLinkExtractor"
    }
}
