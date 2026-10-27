package com.app.core.data

import com.app.core.domain.AppResult
import com.app.core.domain.DomainError
import com.app.core.domain.Ingredient
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeLinkExtractor
import com.app.core.domain.Visibility
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
import java.util.concurrent.TimeUnit

/**
 * Implementación de [RecipeLinkExtractor]:
 *  1. Hace un GET a la URL para extraer texto de meta-tags OpenGraph (og:title, og:description).
 *  2. Envía ese texto a Gemini API con un prompt que fuerza respuesta JSON estructurada.
 *  3. Parsea y devuelve una [Recipe] borrador para que el usuario la revise y complete.
 */
class GeminiLinkExtractor @Inject constructor(
    @Named("gemini_api_key") private val apiKey: String
) : RecipeLinkExtractor {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun extract(url: String): AppResult<Recipe> = withContext(Dispatchers.IO) {
        try {
            val pageText = fetchPageText(url)
            val recipe = callGemini(pageText)
            AppResult.Success(recipe)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Error(DomainError.Unknown(e.message))
        }
    }

    private fun fetchPageText(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (compatible; RecetarioApp/1.0)")
            .build()

        val html = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code} al acceder al link")
            response.body?.string() ?: throw Exception("Respuesta vacía del link")
        }

        val ogTitle = Regex("""<meta[^>]+property=["']og:title["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
            .find(html)?.groupValues?.getOrNull(1)
            ?: Regex("""<title[^>]*>([^<]+)</title>""", RegexOption.IGNORE_CASE).find(html)?.groupValues?.getOrNull(1)
            ?: ""

        val ogDesc = Regex("""<meta[^>]+property=["']og:description["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
            .find(html)?.groupValues?.getOrNull(1)
            ?: Regex("""<meta[^>]+name=["']description["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
            .find(html)?.groupValues?.getOrNull(1)
            ?: ""

        val text = listOf(ogTitle, ogDesc).filter { it.isNotBlank() }.joinToString("\n\n")
        if (text.isBlank()) throw Exception("No se encontró texto en el link")
        return text
    }

    private fun callGemini(pageText: String): Recipe {
        val prompt = """
            Sos un asistente de cocina. A partir del siguiente texto extraído de una página web o red social, 
            identificá si hay una receta y devolvé ÚNICAMENTE un JSON válido con esta estructura (sin texto adicional ni markdown):
            {
              "nombre": "Nombre del plato",
              "porciones": 4,
              "tiempo_minutos": 30,
              "ingredientes": [
                {"cantidad": "200 g", "nombre": "Harina"}
              ],
              "pasos": [
                "Mezclar los ingredientes secos.",
                "Agregar el líquido de a poco."
              ]
            }
            
            Si el texto no contiene información de receta suficiente, devolvé igual el JSON con los campos que puedas inferir y el resto vacío.
            
            Texto a analizar:
            $pageText
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
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val responseText = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Error Gemini API: HTTP ${response.code}")
            response.body?.string() ?: throw Exception("Respuesta vacía de Gemini")
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

        // Limpia el bloque ```json ... ``` que Gemini a veces agrega
        val clean = text
            .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

        val json = JSONObject(clean)
        val ingredientes = json.optJSONArray("ingredientes") ?: JSONArray()
        val pasos = json.optJSONArray("pasos") ?: JSONArray()

        return Recipe(
            id = "",
            name = json.optString("nombre", ""),
            photoUrl = null,
            city = "", country = "",
            prepTimeMinutes = json.optInt("tiempo_minutos", 0),
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
}
