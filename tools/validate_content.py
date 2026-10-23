#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Valida los JSON de app/src/main/assets/content antes de compilar.

Uso (desde la raíz del proyecto):
    python tools/validate_content.py

Solo usa la biblioteca estándar de Python 3.8+. Termina con código 1 si hay errores.
"""
import json
import re
import sys
from pathlib import Path

CONTENT_DIR = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "assets" / "content"
DIFFICULTIES = {"BASICO", "INTERMEDIO", "AVANZADO"}
MODULES = {"M1", "M2"}
DISPLAY = re.compile(r"\$\$(.+?)\$\$", re.DOTALL)
INLINE = re.compile(r"\$(.+?)\$")
TEXT_CMD = re.compile(r"\\(?:text|mathrm|operatorname)\{[^}]*\}")
LATEX_CMD = re.compile(r"\\[A-Za-z]+")

errors, warnings = [], []
seen_topics, seen_questions = {}, {}


def err(where, msg):
    errors.append(f"ERROR   {where}: {msg}")


def warn(where, msg):
    warnings.append(f"AVISO   {where}: {msg}")


def check_markup(where, text):
    if not isinstance(text, str) or not text.strip():
        err(where, "texto vacío o no es texto")
        return
    if text.count("$") % 2 != 0:
        err(where, "cantidad impar de '$' (fórmula sin cerrar o '$' usado para dinero)")
    # Si entre dos '$' queda texto corrido, probablemente se usó '$' para dinero.
    rest = text
    segments = [m.group(1) for m in DISPLAY.finditer(rest)]
    rest = DISPLAY.sub(" ", rest)
    segments += [m.group(1) for m in INLINE.finditer(rest)]
    for seg in segments:
        plain = LATEX_CMD.sub("", TEXT_CMD.sub("", seg))
        if re.search(r"[A-Za-zÁ-ú]{3,}", plain):
            warn(where, f"entre '$' hay texto que no parece fórmula ({seg.strip()[:40]!r}); "
                        "¿usaste '$' para dinero? Escribe 'pesos' o 'CLP'")
            break


def validate_question(where, q):
    for key in ("id", "statement", "options", "correctIndex", "explanation"):
        if key not in q:
            err(where, f"falta el campo '{key}'")
            return
    qid = q["id"]
    if qid in seen_questions:
        err(where, f"id de pregunta repetido '{qid}' (también en {seen_questions[qid]})")
    seen_questions[qid] = where
    opts = q["options"]
    if not isinstance(opts, list) or not 2 <= len(opts) <= 6:
        err(where, "options debe ser una lista de 2 a 6 elementos")
        return
    if len(opts) != 4:
        warn(where, f"la PAES usa 4 alternativas y aquí hay {len(opts)}")
    if len(set(opts)) != len(opts):
        err(where, "hay alternativas repetidas")
    if not isinstance(q["correctIndex"], int) or not 0 <= q["correctIndex"] < len(opts):
        err(where, "correctIndex fuera de rango (empieza en 0)")
    if q.get("difficulty", "BASICO") not in DIFFICULTIES:
        err(where, f"difficulty debe ser una de {sorted(DIFFICULTIES)}")
    check_markup(f"{where}.statement", q["statement"])
    check_markup(f"{where}.explanation", q["explanation"])
    for i, o in enumerate(opts):
        check_markup(f"{where}.options[{i}]", o)


def validate_topic(where, t, filename):
    for key in ("id", "title"):
        if key not in t:
            err(where, f"falta el campo '{key}'")
            return
    tid = t["id"]
    if tid in seen_topics:
        err(where, f"id de tema repetido '{tid}' (también en {seen_topics[tid]})")
    seen_topics[tid] = filename
    if t.get("module", "M1") not in MODULES:
        err(where, f"module debe ser uno de {sorted(MODULES)}")
    if not t.get("lesson"):
        warn(where, "el tema no tiene teoría (lesson)")
    if not t.get("formulas"):
        warn(where, "el tema no tiene fórmulas")
    if len(t.get("questions", [])) < 4:
        warn(where, "tiene menos de 4 preguntas (se recomiendan 30 por tema a futuro)")
    for block in t.get("lesson", []):
        check_markup(f"{where}.lesson", block)
    for j, f in enumerate(t.get("formulas", [])):
        if "name" not in f or "latex" not in f:
            err(f"{where}.formulas[{j}]", "cada fórmula necesita 'name' y 'latex'")
        elif "$" in f["latex"]:
            err(f"{where}.formulas[{j}]", "el campo latex va SIN signos '$'")
    for j, ex in enumerate(t.get("examples", [])):
        if "statement" not in ex or "solution" not in ex:
            err(f"{where}.examples[{j}]", "cada ejemplo necesita 'statement' y 'solution'")
        else:
            check_markup(f"{where}.examples[{j}].statement", ex["statement"])
            check_markup(f"{where}.examples[{j}].solution", ex["solution"])
    for j, m in enumerate(t.get("commonMistakes", [])):
        check_markup(f"{where}.commonMistakes[{j}]", m)
    for j, q in enumerate(t.get("questions", [])):
        validate_question(f"{where}.questions[{j}]", q)


def main():
    files = sorted(CONTENT_DIR.glob("*.json"))
    if not files:
        print(f"No hay archivos .json en {CONTENT_DIR}")
        return 1
    total_t = total_q = 0
    for path in files:
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError as e:
            err(path.name, f"JSON inválido: {e}")
            continue
        if "axis" not in data or "topics" not in data:
            err(path.name, "debe tener 'axis' y 'topics'")
            continue
        for key in ("id", "name"):
            if key not in data["axis"]:
                err(f"{path.name}.axis", f"falta '{key}'")
        for i, t in enumerate(data["topics"]):
            validate_topic(f"{path.name}:{t.get('id', i)}", t, path.name)
            total_t += 1
            total_q += len(t.get("questions", []))
    print("\n".join(errors + warnings))
    print(f"\nArchivos: {len(files)} | Temas: {total_t} | Preguntas: {total_q} | "
          f"Errores: {len(errors)} | Avisos: {len(warnings)}")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
