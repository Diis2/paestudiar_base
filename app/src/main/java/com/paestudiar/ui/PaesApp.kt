package com.paestudiar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.paestudiar.data.ContentPack
import com.paestudiar.data.ContentState
import com.paestudiar.data.Progress
import com.paestudiar.ui.navigation.AppTab
import com.paestudiar.ui.navigation.Routes
import com.paestudiar.ui.navigation.navigateToTab
import com.paestudiar.ui.screens.ExamsScreen
import com.paestudiar.ui.screens.HomeScreen
import com.paestudiar.ui.screens.PracticeHubScreen
import com.paestudiar.ui.screens.PracticeSessionScreen
import com.paestudiar.ui.screens.ProgressScreen
import com.paestudiar.ui.screens.StudyScreen
import com.paestudiar.ui.screens.TopicScreen

@Composable
fun PaesApp(vm: AppViewModel = viewModel(factory = AppViewModel.Factory)) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val contentState by vm.content.collectAsStateWithLifecycle()
    val progress by vm.progress.collectAsStateWithLifecycle()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppTab.entries.forEach { tab ->
                item(
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) },
                    selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                    onClick = { navController.navigateToTab(tab) }
                )
            }
        }
    ) {
        when (val state = contentState) {
            is ContentState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            is ContentState.Error -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No se pudo cargar el contenido")
                Text(state.message)
                Button(onClick = vm::load, modifier = Modifier.padding(top = 16.dp)) { Text("Reintentar") }
            }

            is ContentState.Ready -> AppNavHost(
                navController = navController,
                packs = state.packs,
                progress = progress,
                vm = vm
            )
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    packs: List<ContentPack>,
    progress: Progress,
    vm: AppViewModel
) {
    val topics = remember(packs) { packs.flatMap { it.topics }.associateBy { it.id } }
    val axisByTopic = remember(packs) {
        packs.flatMap { pack -> pack.topics.map { it.id to pack.axis } }.toMap()
    }
    val topicArg = listOf(navArgument(Routes.ARG_TOPIC_ID) { type = NavType.StringType })

    NavHost(
        navController = navController,
        startDestination = AppTab.HOME.route,
        modifier = Modifier.fillMaxSize()
    ) {
        navigation(route = AppTab.HOME.route, startDestination = Routes.HOME) {
            composable(Routes.HOME) {
                HomeScreen(
                    packs = packs,
                    progress = progress,
                    onOpenTopic = { navController.navigate(Routes.topic(it)) },
                    onPractice = { navController.navigate(Routes.session(it)) }
                )
            }
        }

        navigation(route = AppTab.STUDY.route, startDestination = Routes.STUDY) {
            composable(Routes.STUDY) {
                StudyScreen(
                    packs = packs,
                    progress = progress,
                    onOpenTopic = { navController.navigate(Routes.topic(it)) }
                )
            }
            composable(Routes.TOPIC, arguments = topicArg) { entry ->
                val id = entry.arguments?.getString(Routes.ARG_TOPIC_ID).orEmpty()
                val topic = topics[id]
                if (topic != null) {
                    TopicScreen(
                        topic = topic,
                        axisName = axisByTopic[id]?.name.orEmpty(),
                        done = id in progress.lessonsDone,
                        onToggleDone = { vm.toggleLessonDone(id) },
                        onPractice = { navController.navigate(Routes.session(id)) },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        navigation(route = AppTab.PRACTICE.route, startDestination = Routes.PRACTICE) {
            composable(Routes.PRACTICE) {
                PracticeHubScreen(
                    packs = packs,
                    progress = progress,
                    onStart = { navController.navigate(Routes.session(it)) }
                )
            }
            composable(Routes.SESSION, arguments = topicArg) { entry ->
                val id = entry.arguments?.getString(Routes.ARG_TOPIC_ID).orEmpty()
                PracticeSessionScreen(
                    topicId = id,
                    packs = packs,
                    onAnswer = vm::recordAnswer,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        navigation(route = AppTab.EXAMS.route, startDestination = Routes.EXAMS) {
            composable(Routes.EXAMS) { ExamsScreen() }
        }

        navigation(route = AppTab.PROGRESS.route, startDestination = Routes.PROGRESS) {
            composable(Routes.PROGRESS) {
                ProgressScreen(
                    packs = packs,
                    progress = progress,
                    onReset = vm::resetProgress
                )
            }
        }
    }
}
