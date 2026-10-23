# Guía para agregar contenido

Todo el contenido vive en `app/src/main/assets/content/*.json`. **Un archivo = un eje temático.**
La app lee todos los `.json` de esa carpeta al iniciar: para agregar un eje nuevo basta crear otro archivo;
para agregar temas, añádelos al arreglo `topics` del eje correspondiente.

## Flujo de trabajo recomendado

1. Edita o crea el JSON (usa `docs/plantilla_tema.json` como base).
2. Ejecuta `python tools/validate_content.py` desde la raíz del proyecto. Debe terminar con **Errores: 0**.
3. Ejecuta la app y revisa visualmente el tema (teoría, fórmulas y al menos una pregunta).
4. Haz commit.

## Formato del texto (con fórmulas)

| Escribes | Resultado |
|---|---|
| `$\frac{a}{b}$` | fórmula en línea |
| `$$x=\frac{-b\pm\sqrt{b^2-4ac}}{2a}$$` | fórmula centrada en su propia línea |
| `**texto**` | negrita |
| línea en blanco | nuevo párrafo |
| líneas que empiezan con `- ` (en un bloque aparte) | lista con viñetas |

**Reglas importantes**

- **No uses `$` para dinero.** Escribe "25.000 pesos". Los `$` se reservan para fórmulas.
- En JSON, cada `\` debe escribirse como `\\`. Ejemplo: `"$\\frac{3}{4}$"`.
  (Si generas el contenido con un script de Python, usa cadenas `r"..."` y `json.dump`: lo hace solo.)
- En el campo `latex` de las fórmulas **no** van signos `$`.
- Coma decimal: escribe `0{,}35` dentro de una fórmula (las llaves evitan un espacio raro).
- Los ids deben ser únicos y estables (no los cambies después, o se pierde el progreso asociado).

## LaTeX útil

| Quieres | Escribes |
|---|---|
| Fracción | `\frac{a}{b}` (más grande: `\dfrac{a}{b}`) |
| Potencia / subíndice | `x^{2}` / `x_{1}` |
| Raíz | `\sqrt{x}`, `\sqrt[3]{x}` |
| Multiplicación, división | `\cdot`, `\div` |
| Más/menos | `\pm` |
| Comparación | `\le`, `\ge`, `\neq`, `\approx` |
| π, Δ, σ | `\pi`, `\Delta`, `\sigma` |
| Valor absoluto | `|x|` |
| Segmento / periodo | `\overline{AB}` / `0{,}\overline{3}` |
| Combinatoria | `\binom{n}{k}` |
| Texto dentro de fórmula | `\text{casos favorables}` |
| Unidades | `\text{cm}^2` |
| Sistemas | `\begin{cases} x+y=5 \\ x-y=1 \end{cases}` |

## Estructura de un tema

```json
{
  "id": "alg-sistemas",
  "title": "Sistemas de ecuaciones 2×2",
  "summary": "Una línea que se ve en la lista de temas.",
  "order": 5,
  "module": "M1",
  "lesson":   [ "Párrafo 1…", "Párrafo 2…" ],
  "formulas": [ { "name": "Nombre", "latex": "…", "note": "opcional" } ],
  "examples": [ { "statement": "Problema…", "solution": "Solución paso a paso…" } ],
  "commonMistakes": [ "Error típico 1", "Error típico 2" ],
  "questions": [
    {
      "id": "alg-sis-001",
      "statement": "Enunciado…",
      "options": ["A", "B", "C", "D"],
      "correctIndex": 2,
      "explanation": "Por qué es correcta y por qué fallan las demás…",
      "difficulty": "BASICO"
    }
  ]
}
```

- `correctIndex` empieza en **0** (0 = A, 1 = B, 2 = C, 3 = D).
- `difficulty`: `BASICO`, `INTERMEDIO` o `AVANZADO`.
- `module`: `M1` o `M2`.

## Cómo escribir buenas preguntas (estilo PAES)

- Siempre **4 alternativas**, una sola correcta, y distribuye la correcta entre A, B, C y D.
- Los **distractores deben ser errores reales** de los estudiantes (invertir una fracción, sumar porcentajes,
  olvidar el signo…), no números al azar.
- Incluye en `explanation` el **procedimiento** y, cuando aplique, por qué falla el error típico.
- Mezcla dificultades: aprox. 40 % básico, 40 % intermedio, 20 % avanzado.
- Meta por tema: **30 preguntas** (hoy hay 4 por tema).
- Usa preguntas **originales**. Las pruebas oficiales del DEMRE tienen sus propias condiciones de uso;
  úsalas como referencia de estilo, no las copies.
- **Verifica cada resultado.** Calcula la respuesta por otro camino antes de publicar.

## Temas pendientes sugeridos (M1)

Ids propuestos para mantener un orden consistente. Verifica el alcance contra el temario oficial vigente del DEMRE.

| Eje (archivo) | Tema | id sugerido |
|---|---|---|
| Números | Enteros: operaciones, orden, MCM y MCD | `num-enteros` |
| Números | Razones y proporcionalidad | `num-proporcion` |
| Álgebra | Sistemas de ecuaciones 2×2 | `alg-sistemas` |
| Álgebra | Expresiones algebraicas y fracciones algebraicas | `alg-expresiones` |
| Álgebra | Función potencia y modelos de crecimiento | `alg-potencia` |
| Geometría | Transformaciones isométricas y homotecia | `geo-isometrias` |
| Geometría | Plano cartesiano y vectores | `geo-plano` |
| Geometría | Área y perímetro de figuras compuestas | `geo-compuestas` |
| Prob. y est. | Tablas de frecuencia y gráficos | `est-tablas` |
| Prob. y est. | Cuartiles, percentiles y diagramas de caja | `est-posicion` |
| Prob. y est. | Variable aleatoria y modelos de probabilidad | `est-variable` |

Para M2 (fase 2): logaritmos, función exponencial y logarítmica, trigonometría, geometría analítica
y distribuciones binomial y normal.

## Cómo pedirme más contenido

Dime: **tema(s)**, **módulo** (M1/M2), **cantidad de preguntas** y **dificultades**.
Genero el JSON con el mismo formato, verifico cada respuesta con cálculo exacto y reviso que todas las
fórmulas se rendericen sin errores antes de entregarlo. Tú lo pegas en `assets/content/` y ejecutas el validador.
