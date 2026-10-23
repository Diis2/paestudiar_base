package com.paestudiar.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/** Las 5 pestañas de la barra de navegación. */
enum class AppTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Inicio", Icons.Filled.Home),
    STUDY("study", "Estudiar", Icons.AutoMirrored.Filled.List),
    PRACTICE("practice", "Practicar", Icons.Filled.Edit),
    EXAMS("exams", "Ensayos", Icons.Filled.CheckCircle),
    PROGRESS("progress", "Progreso", Icons.Filled.Star)
}

object Routes {
    const val HOME = "home/root"
    const val STUDY = "study/root"
    const val TOPIC = "study/topic/{topicId}"
    const val PRACTICE = "practice/root"
    const val SESSION = "practice/session/{topicId}"
    const val EXAMS = "exams/root"
    const val PROGRESS = "progress/root"

    const val ARG_TOPIC_ID = "topicId"

    fun topic(id: String) = "study/topic/$id"
    fun session(id: String) = "practice/session/$id"
}

fun NavHostController.navigateToTab(tab: AppTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
