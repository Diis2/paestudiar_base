package com.paestudiar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paestudiar.data.ContentPack
import com.paestudiar.data.Progress
import com.paestudiar.data.accuracy
import com.paestudiar.data.stats
import com.paestudiar.data.streak
import com.paestudiar.data.todayEpochDay
import com.paestudiar.data.totalAnswered
import com.paestudiar.ui.components.ScreenScaffold
import com.paestudiar.ui.components.StatCard
import com.paestudiar.ui.theme.PaesColors
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(
    packs: List<ContentPack>,
    progress: Progress,
    onReset: () -> Unit
) {
    var confirmReset by remember { mutableStateOf(false) }
    val accuracyText = progress.accuracy()?.let { "${(it * 100).roundToInt()}%" } ?: "–"

    ScreenScaffold(title = "Progreso") { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("Racha (días)", "${progress.streak(todayEpochDay())}", Modifier.weight(1f))
                    StatCard("Respondidas", "${progress.totalAnswered()}", Modifier.weight(1f))
                    StatCard("Aciertos", accuracyText, Modifier.weight(1f))
                }
            }
            packs.forEachIndexed { index, pack ->
                item(key = "axis-${pack.axis.id}") {
                    Text(
                        pack.axis.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PaesColors.axisColor(index),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                pack.topics.forEach { topic ->
                    item(key = "topic-${topic.id}") {
                        val stats = topic.stats(progress)
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val label = stats.accuracy
                                ?.let { "${(it * 100).roundToInt()}% (${stats.correct}/${stats.answered})" }
                                ?: "Sin intentos"
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(topic.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text(label, style = MaterialTheme.typography.labelMedium)
                            }
                            LinearProgressIndicator(
                                progress = { stats.accuracy ?: 0f },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = { confirmReset = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) { Text("Borrar mi progreso") }
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Borrar progreso") },
            text = { Text("Se eliminarán tus respuestas, racha y temas estudiados. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { onReset(); confirmReset = false }) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("Cancelar") }
            }
        )
    }
}
