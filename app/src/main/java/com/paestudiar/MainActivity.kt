package com.paestudiar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.paestudiar.data.Graph
import com.paestudiar.ui.PaesApp
import com.paestudiar.ui.theme.PaesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Graph.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            PaesTheme {
                PaesApp()
            }
        }
    }
}
