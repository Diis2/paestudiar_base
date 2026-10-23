package com.paestudiar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paestudiar.data.AppConfig
import com.paestudiar.data.ContentPack
import com.paestudiar.data.Progress
import com.paestudiar.data.accuracy
import com.paestudiar.data.recommendTopic
import com.paestudiar.data.streak
import com.paestudiar.data.todayEpochDay
import com.paestudiar.data.totalAnswered
import com.paestudiar.ui.components.ScreenScaffold
import com.paestudiar.ui.components.SectionTitle
import com.paestudiar.ui.components.StatCard
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    packs: List<ContentPack>,
    progress: Progress,
    onOpenTopic: (String) -> Unit,
    onPractice: (String) -> Unit
) {
    val today = remember { todayEpochDay() }
    val streak = progress.streak(today)
    val recommended = remember(packs, progress) { recommendTopic(packs, progress) }
    val totalTopics = packs.sumOf { it.topics.size }
    val accuracyText = progress.accuracy()?.let { "${(it * 100).roundToInt()}%" } ?: "–"

    ScreenScaffold(title = "PAEStudiar") { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Prepárate para la PAES de Matemática",
                style = MaterialTheme.typography.headlineSmall
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Racha (días)", "$streak", Modifier.weight(1f))
                StatCard("Respondidas", "${progress.totalAnswered()}", Modifier.weight(1f))
                StatCard("Aciertos", accuracyText, Modifier.weight(1f))
            }

            if (recommended != null) {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SectionTitle("Tema sugerido")
                        Text(recommended.title, style = MaterialTheme.typography.titleLarge)
                        if (recommended.summary.isNotBlank()) {
                            Text(recommended.summary, style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onOpenTopic(recommended.id) }) { Text("Estudiar") }
                            OutlinedButton(
                                onClick = { onPractice(recommended.id) },
                                enabled = recommended.questions.isNotEmpty()
                            ) { Text("Practicar") }
                        }
                    }
                }
            }

            Button(
                onClick = { onPractice(AppConfig.PRACTICE_ALL) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Práctica mixta (${AppConfig.SESSION_SIZE} preguntas)") }

            Text(
                "Temas estudiados: ${progress.lessonsDone.size} de $totalTopics",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
