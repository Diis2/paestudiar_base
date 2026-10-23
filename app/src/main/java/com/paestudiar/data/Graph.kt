package com.paestudiar.data

import android.content.Context

/** Contenedor de dependencias simple (sin Hilt). Se inicializa en MainActivity. */
object Graph {
    lateinit var content: ContentRepository
        private set
    lateinit var progress: ProgressRepository
        private set

    fun init(context: Context) {
        if (::content.isInitialized) return
        val app = context.applicationContext
        content = ContentRepository(app)
        progress = ProgressRepository(app)
    }
}
