package com.paestudiar.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Muestra texto con fórmulas LaTeX usando KaTeX empaquetado en assets (funciona sin internet).
 *
 * Formato del texto ("markup"):
 *  - $formula$      -> fórmula en línea
 *  - $$formula$$    -> fórmula centrada en su propia línea
 *  - **texto**      -> negrita
 *  - línea en blanco -> nuevo párrafo
 *  - bloque cuyas líneas empiezan con "- " -> lista con viñetas
 *
 * No uses el símbolo $ para dinero: escribe "pesos" o "CLP".
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MathText(markup: String, modifier: Modifier = Modifier, fontSizePx: Int = 16) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val hex = String.format("#%06X", 0xFFFFFF and textColor)
    val html = remember(markup, hex, fontSizePx) { MathHtml.build(markup, hex, fontSizePx) }
    var heightDp by remember { mutableIntStateOf(32) }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp),
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(AndroidColor.TRANSPARENT)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                overScrollMode = WebView.OVER_SCROLL_NEVER
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.blockNetworkLoads = true
                settings.textZoom = 100
                val view = this
                addJavascriptInterface(
                    HeightBridge { h -> view.post { heightDp = h } },
                    "Android"
                )
            }
        },
        update = { view ->
            if (view.tag != html) {
                view.tag = html
                view.loadDataWithBaseURL(
                    "file:///android_asset/katex/",
                    html,
                    "text/html",
                    "utf-8",
                    null
                )
            }
        },
        onRelease = { it.destroy() }
    )
}

internal class HeightBridge(private val callback: (Int) -> Unit) {
    @JavascriptInterface
    fun onHeight(h: Int) {
        callback(h)
    }
}

internal object MathHtml {
    private val DISPLAY = Regex("""\$\$(.+?)\$\$""", RegexOption.DOT_MATCHES_ALL)
    private val INLINE = Regex("""\$(.+?)\$""")
    private val BOLD = Regex("""\*\*(.+?)\*\*""")
    private val PARAGRAPHS = Regex("""\n\s*\n""")

    fun build(markup: String, textColor: String, fontSizePx: Int): String {
        val body = toBodyHtml(markup)
        return """<!DOCTYPE html>
<html><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="katex.min.css">
<style>
html,body{margin:0;padding:0;background:transparent;}
body{color:${textColor};font-family:sans-serif;font-size:${fontSizePx}px;line-height:1.5;-webkit-text-size-adjust:100%;}
#c{display:flow-root;}
p{margin:0 0 10px 0;}
ul{margin:0 0 10px 0;padding-left:22px;}
li{margin-bottom:4px;}
.katex-display{margin:8px 0;overflow-x:auto;overflow-y:hidden;}
</style></head>
<body><div id="c">${body}</div>
<script src="katex.min.js"></script>
<script src="auto-render.min.js"></script>
<script>
(function(){
  function report(){
    try { Android.onHeight(Math.ceil(document.getElementById('c').getBoundingClientRect().height)); } catch(e) {}
  }
  function render(){
    try {
      renderMathInElement(document.getElementById('c'), {
        delimiters: [
          {left: '\\[', right: '\\]', display: true},
          {left: '\\(', right: '\\)', display: false}
        ],
        throwOnError: false
      });
    } catch(e) {}
    report();
    if (document.fonts && document.fonts.ready) { document.fonts.ready.then(report); }
    setTimeout(report, 200);
  }
  if (document.readyState === 'complete') { render(); } else { window.addEventListener('load', render); }
})();
</script></body></html>"""
    }

    private fun toBodyHtml(markup: String): String {
        var s = markup.trim()
        s = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        s = DISPLAY.replace(s) { "\\[" + it.groupValues[1] + "\\]" }
        s = INLINE.replace(s) { "\\(" + it.groupValues[1] + "\\)" }
        s = BOLD.replace(s) { "<strong>" + it.groupValues[1] + "</strong>" }

        val out = StringBuilder()
        for (block in s.split(PARAGRAPHS)) {
            val lines = block.trim().lines().filter { it.isNotBlank() }
            if (lines.isEmpty()) continue
            if (lines.all { it.trimStart().startsWith("- ") }) {
                out.append("<ul>")
                lines.forEach { out.append("<li>").append(it.trimStart().removePrefix("- ")).append("</li>") }
                out.append("</ul>")
            } else {
                out.append("<p>").append(lines.joinToString("<br>")).append("</p>")
            }
        }
        return out.toString()
    }
}
