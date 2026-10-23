package com.paestudiar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paestudiar.ui.components.ScreenScaffold

/** Pendiente: ensayos con cronómetro y puntaje estimado (fase 5 de la hoja de ruta). */
@Composable
fun ExamsScreen() {
    ScreenScaffold(title = "Ensayos") { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Ensayos tipo PAES", style = MaterialTheme.typography.titleLarge)
            Text("Aquí irán los simulacros con cronómetro, corrección automática y puntaje estimado.")
            Text("Por ahora puedes usar la pestaña Practicar con la opción de práctica mixta.")
        }
    }
}
