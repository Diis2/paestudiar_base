package com.paestudiar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paestudiar.data.AppConfig
import com.paestudiar.data.ContentPack
import com.paestudiar.data.Progress
import com.paestudiar.data.Question
import com.paestudiar.data.stats
import com.paestudiar.ui.components.MathText
import com.paestudiar.ui.components.ScreenScaffold
import com.paestudiar.ui.theme.PaesColors
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeHubScreen(
    packs: List<ContentPack>,
    progress: Progress,
    onStart: (String) -> Unit
) {
    val total = packs.sumOf { pack -> pack.topics.sumOf { it.questions.size } }
    ScreenScaffold(title = "Practicar") { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "mixed") {
                Card(
                    onClick = { onStart(AppConfig.PRACTICE_ALL) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Práctica mixta", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${AppConfig.SESSION_SIZE} preguntas al azar de todos los temas ($total disponibles)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
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
                items(pack.topics.filter { it.questions.isNotEmpty() }, key = { it.id }) { topic ->
                    val stats = topic.stats(progress)
                    Card(onClick = { onStart(topic.id) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Text(topic.title, style = MaterialTheme.typography.titleSmall)
                            val acc = stats.accuracy?.let { " · aciertos ${(it * 100).roundToInt()}%" }.orEmpty()
                            Text(
                                "${topic.questions.size} preguntas$acc",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildQuestions(topicId: String, packs: List<ContentPack>): List<Question> {
    val topics = packs.flatMap { it.topics }
    return if (topicId == AppConfig.PRACTICE_ALL) {
        topics.flatMap { it.questions }.shuffled().take(AppConfig.SESSION_SIZE)
    } else {
        topics.firstOrNull { it.id == topicId }?.questions.orEmpty().shuffled()
    }
}

@Composable
fun PracticeSessionScreen(
    topicId: String,
    packs: List<ContentPack>,
    onAnswer: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val title = if (topicId == AppConfig.PRACTICE_ALL) {
        "Práctica mixta"
    } else {
        packs.flatMap { it.topics }.firstOrNull { it.id == topicId }?.title ?: "Práctica"
    }
    var round by remember { mutableIntStateOf(0) }
    val questions = remember(topicId, round, packs) { buildQuestions(topicId, packs) }
    var index by remember(topicId, round) { mutableIntStateOf(0) }
    var correctCount by remember(topicId, round) { mutableIntStateOf(0) }
    var selected by remember(topicId, round, index) { mutableStateOf<Int?>(null) }

    ScreenScaffold(title = title, onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                questions.isEmpty() -> Text("Aún no hay preguntas para este tema.")

                index >= questions.size -> ResultBlock(
                    correct = correctCount,
                    total = questions.size,
                    onRetry = { round++ },
                    onExit = onBack
                )

                else -> {
                    val q = questions[index]
                    QuestionBlock(
                        q = q,
                        position = index,
                        total = questions.size,
                        selected = selected,
                        onSelect = { i ->
                            if (selected == null) {
                                selected = i
                                val ok = i == q.correctIndex
                                if (ok) correctCount++
                                onAnswer(q.id, ok)
                            }
                        },
                        onNext = { index++ }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionBlock(
    q: Question,
    position: Int,
    total: Int,
    selected: Int?,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit
) {
    Text("Pregunta ${position + 1} de $total", style = MaterialTheme.typography.labelLarge)
    LinearProgressIndicator(
        progress = { (position + if (selected != null) 1 else 0).toFloat() / total },
        modifier = Modifier.fillMaxWidth()
    )

    val markup = remember(q) {
        q.statement + "\n\n" +
            q.options.mapIndexed { i, o -> "**${'A' + i})** $o" }.joinToString("\n")
    }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Box(Modifier.padding(16.dp)) { MathText(markup) }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        q.options.indices.forEach { i ->
            val container: Color = when {
                selected == null -> MaterialTheme.colorScheme.primary
                i == q.correctIndex -> PaesColors.Correct
                i == selected -> PaesColors.Wrong
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val content: Color = when {
                selected == null -> MaterialTheme.colorScheme.onPrimary
                i == q.correctIndex || i == selected -> Color.White
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Button(
                onClick = { onSelect(i) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content)
            ) { Text("${'A' + i}") }
        }
    }

    if (selected != null) {
        val ok = selected == q.correctIndex
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = (if (ok) PaesColors.Correct else PaesColors.Wrong).copy(alpha = 0.15f)
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    if (ok) "¡Correcto!" else "Incorrecto. La respuesta correcta es la ${'A' + q.correctIndex}.",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                MathText(q.explanation)
            }
        }
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text(if (position + 1 < total) "Siguiente" else "Ver resultado")
        }
    }
}

@Composable
private fun ResultBlock(correct: Int, total: Int, onRetry: () -> Unit, onExit: () -> Unit) {
    val pct = if (total == 0) 0 else (correct * 100f / total).roundToInt()
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resultado", style = MaterialTheme.typography.titleLarge)
            Text("$correct de $total correctas ($pct%)", style = MaterialTheme.typography.headlineSmall)
            Text(
                when {
                    pct >= 80 -> "Muy bien: este tema va por buen camino."
                    pct >= 50 -> "Vas avanzando. Repasa la teoría y vuelve a intentar."
                    else -> "Conviene repasar la teoría de este tema antes de seguir."
                }
            )
        }
    }
    Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("Intentar de nuevo") }
    OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
}
