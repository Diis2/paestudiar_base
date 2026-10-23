package com.paestudiar.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Lee todos los .json de assets/content. Para agregar contenido basta con
 * añadir un archivo nuevo (o temas nuevos en uno existente): no hay que tocar código.
 */
class ContentRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun loadAll(): List<ContentPack> = withContext(Dispatchers.IO) {
        val files = context.assets.list(AppConfig.CONTENT_DIR).orEmpty()
            .filter { it.endsWith(".json") }
            .sorted()
        files.map { name ->
            try {
                val text = context.assets.open("${AppConfig.CONTENT_DIR}/$name")
                    .bufferedReader().use { it.readText() }
                json.decodeFromString(ContentPack.serializer(), text)
            } catch (e: Exception) {
                throw IllegalStateException("Error en content/$name: ${e.message}", e)
            }
        }
            .sortedBy { it.axis.order }
            .map { pack -> pack.copy(topics = pack.topics.sortedBy { it.order }) }
    }
}
