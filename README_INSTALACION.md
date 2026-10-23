# PAEStudiar: base del proyecto

Esta base reemplaza el contenido que generó Android Studio con la plantilla "Navigation UI Activity".
Usa Jetpack Compose, Navigation Compose, DataStore y contenido en JSON. **No usa Room, KSP ni Hilt** (ver "Decisiones").

> **Importante:** no pude compilar este proyecto en mi entorno (no tengo acceso al SDK de Android ni a Google Maven).
> Revisé el código con cuidado y validé el contenido matemático y las fórmulas, pero la primera compilación
> es tuya. Si aparece cualquier error, copia el mensaje completo de la pestaña *Build* y se corrige rápido.

## Pasos

1. **Respalda tu proyecto** (si ya está en Git, haz un commit antes).
2. **Reemplaza estos 3 archivos** por los de este paquete (son tus mismos archivos con las líneas nuevas agregadas):
   - `gradle/libs.versions.toml`
   - `build.gradle.kts` (el de la raíz)
   - `app/build.gradle.kts`
3. **Copia el código**: la carpeta `app/src/main/java/com/paestudiar/` de este paquete a la misma ruta de tu proyecto.
   Cuando pregunte, acepta reemplazar `MainActivity.kt`.
4. **Copia los recursos**: la carpeta `app/src/main/assets/` completa (si no existe en tu proyecto, se crea sola).
   Debe quedar así:
   ```
   app/src/main/assets/
   ├── content/   (4 archivos .json)
   └── katex/     (katex.min.js, katex.min.css, auto-render.min.js, fonts/)
   ```
5. **Opcional:** puedes borrar los archivos de tema que generó la plantilla en `ui/theme/` (`Color.kt`, `Theme.kt`, `Type.kt`);
   esta base usa su propio `PaesTheme.kt`. Si los dejas, no molestan.
6. En Android Studio: **File ▸ Sync Project with Gradle Files**, y luego **Run**.
7. Copia también `tools/` y `docs/` a la raíz de tu repositorio (no afectan la compilación).

## Qué deberías ver

- Barra inferior con 5 pestañas: **Inicio, Estudiar, Practicar, Ensayos, Progreso**.
- **Estudiar:** 4 ejes y 11 temas con teoría, fórmulas, ejemplos resueltos y errores frecuentes.
- **Practicar:** 44 preguntas con alternativas A–D, corrección inmediata y explicación.
- **Progreso:** aciertos por tema, racha y botón para borrar el progreso.
- **Ensayos:** pantalla pendiente (fase 5 de la hoja de ruta).

## Si algo falla

| Síntoma | Causa probable |
|---|---|
| Las fórmulas se ven como texto (`\frac...`) o no aparecen | Revisa que `assets/katex/` tenga los 3 archivos y la carpeta `fonts/`. Haz *Build ▸ Clean Project* y vuelve a ejecutar. |
| Pantalla "No se pudo cargar el contenido" | El mensaje indica qué archivo JSON falló. Ejecuta `python tools/validate_content.py`. |
| `Unresolved reference` en `serialization` o `navigation` | No se sincronizó Gradle, o el `libs.versions.toml` no se reemplazó completo. |
| Error de versión de Kotlin/AGP | Envíame el mensaje exacto del *Build*. |
| Un ícono no existe | Los íconos están en `ui/navigation/Destinations.kt`; puedes cambiarlos por otros de `Icons.Filled`. |

## Antes de publicar (build *release*)

Tu configuración de *release* usa optimización (R8). El componente `MathText` usa un puente JavaScript
(`@JavascriptInterface`). Si en un build *release* las fórmulas dejan de ajustar su altura, agrega esta regla de ProGuard:

```
-keepclassmembers class com.paestudiar.ui.components.HeightBridge {
    @android.webkit.JavascriptInterface <methods>;
}
```

## Decisiones de esta base

- **Sin Room/KSP/Hilt por ahora.** AGP 9 integra Kotlin de forma propia y KSP todavía requiere versiones específicas
  (KSP 2.3.x). Para que la primera compilación sea confiable, el progreso se guarda con DataStore y las dependencias
  se arman a mano en `Graph.kt`. Las pantallas no dependen de eso: acceden a `ContentRepository` y `ProgressRepository`,
  así que migrar a Room/Hilt más adelante no obliga a reescribirlas.
- **Contenido en JSON.** Agregar temas o preguntas no requiere tocar código (ver `docs/GUIA_AGREGAR_CONTENIDO.md`).
- **Fórmulas con KaTeX** empaquetado en la app (licencia MIT, incluida en `assets/katex/`): funciona sin internet y
  no necesita permiso de INTERNET (de hecho, el WebView tiene la red bloqueada).
- **minSdk 24** se mantiene: por eso la racha usa cálculo de días propio en vez de `java.time`.

## Estructura del código

```
com.paestudiar
├── MainActivity.kt
├── data/        Models, Progress, ContentRepository, ProgressRepository, Graph, AppConfig
└── ui/
    ├── AppViewModel.kt, PaesApp.kt
    ├── navigation/   Destinations.kt (pestañas y rutas)
    ├── screens/      Home, Study(+Topic), Practice(+Session), Exams, Progress
    ├── components/   MathText (KaTeX), Common
    └── theme/        PaesTheme
```
