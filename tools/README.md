# Niyam content tooling

`generate_scripts.py` regenerates the 6 derived script fields (telugu, tamil,
kannada, bengali, gujarati, roman) in `app/src/main/assets/content/mantras.json`
from each entry's `text.devanagari` master, via Aksharamukha.

- Setup: `python3.11 -m venv .venv && .venv/bin/pip install -r requirements.txt`
- Regenerate: `.venv/bin/python generate_scripts.py`
- Verify no drift: `.venv/bin/python generate_scripts.py --check`

> **Python version:** the venv MUST be built with Python **3.9–3.11**, not 3.12+.
> Aksharamukha 2.x (all published releases, incl. 2.2.1 and 2.3) ships a dead
> `from ast import Str` import in `PreProcess.py`; `ast.Str` was removed in
> Python 3.12, so the package fails to import on 3.12/3.13/3.14. This machine's
> default `python3` is 3.14, so the venv is pinned to `/opt/homebrew/bin/python3.11`
> (Python 3.11.15). `requirements.txt` stays at `aksharamukha==2.2.1` — the pin
> is fine; only the interpreter matters.

## Config notes (frozen at calibration, 2026-06-10)

Source script: `Devanagari`. Each field is `transliterate.process(SRC, target,
devanagari, pre_options=[...], post_options=[...]).strip()`.

| field    | target            | pre | post                                          | rationale |
|----------|-------------------|-----|-----------------------------------------------|-----------|
| telugu   | `Telugu`          | —   | —                                             | Default Telugu output; matches stotranidhi/Telugu-Wikipedia rendering verbatim. |
| tamil    | `Tamil`           | —   | `TamilRemoveApostrophe`, `TamilRemoveNumbers` | Tamil-with-Grantha (ஜ ஷ ஸ ஹ) per founder-approved spec. `TamilRemoveNumbers` strips Aksharamukha's superscript aspirate markers (⁴ etc.); `TamilRemoveApostrophe` strips the anusvara apostrophe (ʼ). Grantha consonants are preserved. |
| kannada  | `Kannada`         | —   | —                                             | Default Kannada output; matches Kannada-Wikipedia rendering verbatim. |
| bengali  | `Bengali`         | —   | —                                             | Default Bengali output. Bengali reuses the Devanagari dandas । ॥, so they pass through unchanged. |
| gujarati | `Gujarati`        | —   | —                                             | Default Gujarati output; proper ૐ om glyph emitted. |
| roman    | `RomanColloquial` | —   | —                                             | Readable, **no diacritics** per spec §3 ("Om Bhur Bhuvah Svah" style, NOT "ōṁ bhūr bhuvaḥ svaḥ"). Chosen over `RomanReadable`, which adds visarga apostrophes (`bhuvah'`, `svah'`) and doubled long vowels (`bhoor`, `dheemahi`); `RomanColloquial` produces clean `Om bhurbhuvah svah ... dhiyo yo nah prachodayat`. Other roman schemes (IAST/ISO/HK/Itrans) all carry diacritics or case-markers and were rejected. |

### Known, accepted convention details (not bugs)

- **Dandas:** Aksharamukha maps the Devanagari `।`→`.` and `॥`→`..` for Telugu,
  Tamil, Kannada, Gujarati, and roman (script-default). Bengali keeps `।`/`॥`
  (same characters in the Bengali block). Deterministic; carried from the master.
- **Tamil visarga** renders as `꞉` (U+A789 MODIFIER LETTER COLON). Some devotional
  sites use a plain `:`; both are accepted Tamil visarga conventions. The two
  `Tamil*` post-options intentionally leave it (it is neither an apostrophe nor a number).
- **Om glyphs:** Telugu/Kannada emit the spelled-out `ఓం`/`ಓಂ`; Tamil/Gujarati/Bengali
  emit dedicated om glyphs `ௐ`/`ૐ`/`ওঁ`. All standard.

Record any config change here AND re-run the spot-check (docs/superpowers/test-reports/).

Aksharamukha is AGPL — authoring-time tool only; never ships in the APK.
