package com.paestudiar.data

import kotlinx.serialization.Serializable

/** Un archivo JSON de assets/content = un eje temático con sus temas. */
@Serializable
data class ContentPack(val axis: Axis, val topics: List<Topic>)

@Serializable
data class Axis(
    val id: String,
    val name: String,
    val description: String = "",
    val order: Int = 0
)

@Serializable
data class Topic(
    val id: String,
    val title: String,
    val summary: String = "",
    val order: Int = 0,
    val module: String = "M1",
    /** Bloques de teoría. Se separan con línea en blanco al mostrarse. */
    val lesson: List<String> = emptyList(),
    val formulas: List<Formula> = emptyList(),
    val examples: List<Example> = emptyList(),
    val commonMistakes: List<String> = emptyList(),
    val questions: List<Question> = emptyList()
)

@Serializable
data class Formula(
    val name: String,
    val latex: String,
    val note: String = ""
)

@Serializable
data class Example(
    val statement: String,
    val solution: String
)

@Serializable
data class Question(
    val id: String,
    val statement: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val difficulty: String = "BASICO"
)

/** Estado de carga del contenido. */
sealed interface ContentState {
    data object Loading : ContentState
    data class Ready(val packs: List<ContentPack>) : ContentState
    data class Error(val message: String) : ContentState
}
