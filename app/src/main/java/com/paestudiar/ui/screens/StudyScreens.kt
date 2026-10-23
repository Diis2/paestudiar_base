package com.paestudiar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paestudiar.data.Axis
import com.paestudiar.data.ContentPack
import com.paestudiar.data.Progress
import com.paestudiar.data.Topic
import com.paestudiar.data.stats
import com.paestudiar.ui.components.MathText
import com.paestudiar.ui.components.ScreenScaffold
import com.paestudiar.ui.components.SectionTitle
import com.paestudiar.ui.theme.PaesColors
import kotlin.math.roundToInt

@Composable
fun StudyScreen(
    packs: List<ContentPack>,
    progress: Progress,
    onOpenTopic: (String) -> Unit
) {
    ScreenScaffold(title = "Estudiar") { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            packs.forEachIndexed { index, pack ->
                item(key = "axis-${pack.axis.id}") {
                    AxisHeader(pack.axis, PaesColors.axisColor(index))
                }
                items(pack.topics, key = { it.id }) { topic ->
                    TopicCard(topic, progress, onClick = { onOpenTopic(topic.id) })
                }
            }
        }
    }
}

@Composable
private fun AxisHeader(axis: Axis, color: Color) {
    Column(Modifier.padding(top = 8.dp)) {
        Text(
            axis.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        if (axis.description.isNotBlank()) {
            Text(axis.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopicCard(topic: Topic, progress: Progress, onClick: () -> Unit) {
    val stats = topic.stats(progress)
    val done = topic.id in progress.lessonsDone
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(topic.title, style = MaterialTheme.typography.titleMedium)
            if (topic.summary.isNotBlank()) {
                Text(topic.summary, style = MaterialTheme.typography.bodySmall)
            }
            val acc = stats.accuracy?.let { " · aciertos ${(it * 100).roundToInt()}%" }.orEmpty()
            Text(
                (if (done) "Estudiado · " else "") + "${topic.questions.size} preguntas" + acc,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TopicScreen(
    topic: Topic,
    axisName: String,
    done: Boolean,
    onToggleDone: () -> Unit,
    onPractice: () -> Unit,
    onBack: () -> Unit
) {
    ScreenScaffold(title = topic.title, onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(axisName, style = MaterialTheme.typography.labelLarge)

            if (topic.lesson.isNotEmpty()) {
                SectionTitle("Teoría")
                MathText(topic.lesson.joinToString("\n\n"))
            }

            if (topic.formulas.isNotEmpty()) {
                SectionTitle("Fórmulas")
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        MathText(
                            topic.formulas.joinToString("\n\n") { f ->
                                val note = if (f.note.isBlank()) "" else "\n\n${f.note}"
                                "**${f.name}**\n\n\$\$${f.latex}\$\$$note"
                            }
                        )
                    }
                }
            }

            if (topic.examples.isNotEmpty()) {
                SectionTitle("Ejemplos resueltos")
                topic.examples.forEach { ex ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            MathText("**Problema:** ${ex.statement}\n\n**Solución:** ${ex.solution}")
                        }
                    }
                }
            }

            if (topic.commonMistakes.isNotEmpty()) {
                SectionTitle("Errores frecuentes")
                MathText(topic.commonMistakes.joinToString("\n") { "- $it" })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onToggleDone, modifier = Modifier.weight(1f)) {
                    Text(if (done) "Estudiado ✓" else "Marcar como estudiado")
                }
                Button(
                    onClick = onPractice,
                    enabled = topic.questions.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) { Text("Practicar") }
            }
        }
    }
}
