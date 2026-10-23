package com.paestudiar.data

import kotlinx.serialization.Serializable
import java.util.Calendar

@Serializable
data class QuestionStat(val correct: Int = 0, val wrong: Int = 0)

@Serializable
data class Progress(
    val questionStats: Map<String, QuestionStat> = emptyMap(),
    val lessonsDone: Set<String> = emptySet(),
    /** Días de estudio como "día epoch" local (ver todayEpochDay). */
    val studyDays: Set<Long> = emptySet()
)

data class TopicStats(val answered: Int, val correct: Int) {
    val accuracy: Float? get() = if (answered == 0) null else correct.toFloat() / answered
}

/** Día actual (hora local) expresado como número de días desde 1970. */
fun todayEpochDay(): Long {
    val c = Calendar.getInstance()
    return (c.timeInMillis + c.timeZone.getOffset(c.timeInMillis)) / 86_400_000L
}

fun Topic.stats(progress: Progress): TopicStats {
    var answered = 0
    var correct = 0
    for (q in questions) {
        val s = progress.questionStats[q.id] ?: continue
        answered += s.correct + s.wrong
        correct += s.correct
    }
    return TopicStats(answered, correct)
}

fun Progress.totalAnswered(): Int = questionStats.values.sumOf { it.correct + it.wrong }
fun Progress.totalCorrect(): Int = questionStats.values.sumOf { it.correct }
fun Progress.accuracy(): Float? {
    val total = totalAnswered()
    return if (total == 0) null else totalCorrect().toFloat() / total
}

fun Progress.withAnswer(questionId: String, correct: Boolean, day: Long): Progress {
    val old = questionStats[questionId] ?: QuestionStat()
    val updated = if (correct) old.copy(correct = old.correct + 1) else old.copy(wrong = old.wrong + 1)
    return copy(questionStats = questionStats + (questionId to updated), studyDays = studyDays + day)
}

fun Progress.withLessonToggled(topicId: String, day: Long): Progress =
    if (topicId in lessonsDone) copy(lessonsDone = lessonsDone - topicId)
    else copy(lessonsDone = lessonsDone + topicId, studyDays = studyDays + day)

/** Días consecutivos de estudio hasta hoy (o hasta ayer si hoy aún no estudia). */
fun Progress.streak(today: Long): Int {
    var d = if (today in studyDays) today else today - 1
    var n = 0
    while (d in studyDays) {
        n++
        d--
    }
    return n
}

/** Tema sugerido: primero el más débil; si no hay, el primero sin estudiar. */
fun recommendTopic(packs: List<ContentPack>, progress: Progress): Topic? {
    val all = packs.flatMap { it.topics }
    if (all.isEmpty()) return null
    val weak = all
        .filter {
            val s = it.stats(progress)
            s.answered >= AppConfig.WEAK_MIN_ATTEMPTS && (s.accuracy ?: 1f) < AppConfig.WEAK_ACCURACY
        }
        .minByOrNull { it.stats(progress).accuracy ?: 1f }
    return weak
        ?: all.firstOrNull { it.id !in progress.lessonsDone }
        ?: all.minByOrNull { it.stats(progress).accuracy ?: 0f }
}
