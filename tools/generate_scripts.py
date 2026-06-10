#!/usr/bin/env python3
"""Regenerate derived script fields in mantras.json from each entry's
Devanagari master. Deterministic: same input + same config = same output.

Usage:  .venv/bin/python generate_scripts.py [--check]
  --check : verify derived fields match what would be generated (CI-style); exit 1 on drift.

Config is FROZEN after calibration (Step 5.5) — do not edit TARGETS without
re-running the spot-check (plan Task 13)."""

import argparse
import json
import sys
from pathlib import Path

from aksharamukha import transliterate

MANTRAS_JSON = Path(__file__).resolve().parent.parent / "app/src/main/assets/content/mantras.json"

SRC = "Devanagari"

# field -> (aksharamukha target, pre_options, post_options)
# Tamil: Aksharamukha's Tamil target uses Grantha consonants per standard
# devotional convention. Calibration (Step 5.5) locks post_options.
# Roman: "RomanColloquial" chosen over "RomanReadable" — produces the
# diacritic-free "Om Bhur Bhuvah Svah" style required by spec §3 with no
# visarga apostrophes or doubled long vowels. See tools/README.md.
TARGETS = {
    "telugu":   ("Telugu",          [], []),
    "tamil":    ("Tamil",           [], ["TamilRemoveApostrophe", "TamilRemoveNumbers"]),
    "kannada":  ("Kannada",         [], []),
    "bengali":  ("Bengali",         [], []),
    "gujarati": ("Gujarati",        [], []),
    "roman":    ("RomanColloquial", [], []),
}


def derive(devanagari: str) -> dict:
    out = {}
    for field, (target, pre, post) in TARGETS.items():
        out[field] = transliterate.process(
            SRC, target, devanagari, pre_options=pre, post_options=post
        ).strip()
    return out


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    if not MANTRAS_JSON.exists():
        print(f"error: {MANTRAS_JSON} not found (create it in plan Task 6 first)", file=sys.stderr)
        return 2

    data = json.loads(MANTRAS_JSON.read_text(encoding="utf-8"))
    drift = []
    for m in data["mantras"]:
        try:
            derived = derive(m["text"]["devanagari"])
        except KeyError as e:
            print(f"error: entry {m.get('id', '<no id>')} missing key {e}", file=sys.stderr)
            return 2
        for field, value in derived.items():
            if m["text"].get(field) != value:
                drift.append(f'{m["id"]}.text.{field}')
                m["text"][field] = value

    if args.check:
        if drift:
            print("DRIFT in derived fields:\n  " + "\n  ".join(drift))
            return 1
        print("OK: all derived fields match generation config.")
        return 0

    MANTRAS_JSON.write_text(
        json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(f"Regenerated {len(drift)} field(s) across {len(data['mantras'])} entries.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
