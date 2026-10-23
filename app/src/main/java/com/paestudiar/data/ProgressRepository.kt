package com.paestudiar.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.progressDataStore by preferencesDataStore(name = "progress")

/**
 * Guarda el progreso como JSON en DataStore. Detrás de esta clase se puede
 * cambiar a Room más adelante sin tocar las pantallas.
 */
class ProgressRepository(private val context: Context) {

    private val key = stringPreferencesKey("progress_json")
    private val json = Json { ignoreUnknownKeys = true }

    val progress: Flow<Progress> = context.progressDataStore.data.map { prefs -> decode(prefs[key]) }

    suspend fun update(transform: (Progress) -> Progress) {
        context.progressDataStore.edit { prefs ->
            val updated = transform(decode(prefs[key]))
            prefs[key] = json.encodeToString(Progress.serializer(), updated)
        }
    }

    suspend fun reset() {
        context.progressDataStore.edit { it.remove(key) }
    }

    private fun decode(raw: String?): Progress {
        if (raw == null) return Progress()
        return try {
            json.decodeFromString(Progress.serializer(), raw)
        } catch (e: Exception) {
            Progress()
        }
    }
}
