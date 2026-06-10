# Content spot-check — generated scripts vs native devotional sources

- **Date:** 2026-06-10
- **Tool config:** `tools/generate_scripts.py` `TARGETS` @ `5aa372b` (frozen post-calibration; `--check` gate green over all 26 entries)
- **Sample:** `gayatri`, `vishnu-sahasranama-opening`, `hanuman-chalisa-opening` × {telugu, tamil, kannada, bengali, gujarati} + roman pass over all 26
- **Method:** For each (entry, script) pair, the derived field was read from `app/src/main/assets/content/mantras.json` and compared character-by-character (whitespace and danda style normalized) against an independent native-script devotional rendering. Differences classified **match** / **convention** / **error**. Awadhi (Chalisa) in southern scripts, which is essentially unpublished, was verified mechanically (script-block purity + nasalization-count parity against the Devanagari master) where no native source exists.
- **Carry-forwards honored:** vignanam.org / stotranidhi.com 403 automated fetches — surfaced via web search snippets and alternative native sources (hindunidhi, te.wikisource, aanmeegam, gu.wikisource, mantramaya, dedicated Bengali Chalisa sites). Bengali + Gujarati given priority (could not be cross-checked at calibration). Accepted conventions re-confirmed: Tamil visarga `꞉` (U+A789), Tamil om `ௐ`, roman = RomanColloquial (no diacritics).

## Verdict table

| Entry | Script | Source used | Verdict | Notes |
|---|---|---|---|---|
| gayatri | telugu | te.wikipedia.org/wiki/గాయత్రీ_మంత్రం | match | Glyphs identical; native runs words together (భూర్భువస్వః, ధియోయోనఃప్రచోదయాత్) — whitespace only. |
| gayatri | tamil | aanmeegam.org Gayatri-for-all-gods | convention | Native uses `ஓம்` + visarga `:`; ours uses `ௐ` + `꞉` (U+A789). Pre-accepted conventions. Letters identical. |
| gayatri | kannada | vignanam.org/kannada Gayatri (via search) + kn devotional | match | Core letters identical incl. ಭೂರ್ಭುವಃ ಸ್ವಃ, ತತ್ಸವಿತುರ್ವರೇಣ್ಯಂ, ಪ್ರಚೋದಯಾತ್. |
| gayatri | **bengali** | agniveerbangla.org / boldsky / bn.wikipedia | match (1 variant) | ওঁ (candrabindu-om), ভূর্ভুবঃ স্বঃ, ধিয়ো, প্রচোদয়াৎ all match. Variant: some sources write `য়ো` (ya+nukta) vs our `যো` — both attested; ours is the direct cognate of Devanagari यो. |
| gayatri | **gujarati** | mantramaya.com + gu.wikipedia + webdunia | convention | ૐ ભૂર્ભુવઃ સ્વઃ … ધિયો યો નઃ પ્રચોદયાત્ matches. Native `વરેણ્યમ્` (halanta-m) vs ours `વરેણ્યં` (anusvara) — anusvara-vs-explicit-nasal convention. |
| vishnu-sahasranama-opening | telugu | te.wikisource.org Vishṇu Sahasranāma | match | విశ్వం విష్ణుర్వషట్కారో భూతభవ్యభవత్ప్రభుః matches; avagraha ఽ present in క్షేత్రజ్ఞోఽక్షర; మంగలం (anusvara) matches ours. |
| vishnu-sahasranama-opening | tamil | aanmeegam.org VSN lyrics | convention | Grantha letters (ஶ ஷ ஸ க்ஷ) as ours; visarga `:`/`꞉`; avagraha as parenthetical `(அ)` as ours; om `ஓம்` vs `ௐ`. All pre-accepted. Letter stream identical. |
| vishnu-sahasranama-opening | kannada | vedadhara.com + vignanam.org/kannada (via search) | match | ಓಂ ವಿಶ್ವಂ … ಭೂತಭವ್ಯಭವತ್ಪ್ರಭುಃ matches; conjunct-dense clusters render identically. |
| vishnu-sahasranama-opening | **bengali** | hindunidhi.com VSN Bengali | convention | Content identical incl. avagraha ঽ (ক্ষেত্রজ্ঞোঽক্ষর, পদ্মনাভোঽমর) and মুক্তানাং. Difference: native writes `ভবত্প্রভুঃ` (full ত + virama); ours `ভবৎপ্রভুঃ` (khanda-ta ৎ). khanda-ta vs ta+halant — both valid Bengali orthography for pre-consonantal dental-t. |
| vishnu-sahasranama-opening | **gujarati** | gujarati.webdunia.com VSN | convention | ભૂતભવ્યભવત્પ્રભુઃ, ક્ષેત્રજ્ઞોઽક્ષર (avagraha), શ્રીમાન્ all match exactly. Difference: native `સમ્ભવો` / `સ્વયમ્ભૂઃ` (conjunct મ્ભ) vs ours `સંભવો` / `સ્વયંભૂઃ` (anusvara) — anusvara-vs-conjunct-nasal convention. |
| hanuman-chalisa-opening | telugu | no native source found | match (mechanical) | Awadhi in Telugu script is unpublished. Mechanical: script block 100% pure Telugu; nasalization parity vs Devanagari master exact — candrabindu (U+0C00) ×4, anusvara (U+0C02) ×8 = master's ँ×4 + ं×8. |
| hanuman-chalisa-opening | tamil | no native source found | match (mechanical) | Unpublished in Tamil. Mechanical: script block 100% pure Tamil (+ `꞉`). Tamil has no anusvara/candrabindu sign — all 12 nasal positions correctly emulated as ம் (ma+virama), e.g. பரனஉம், திஹும், காம்தே. |
| hanuman-chalisa-opening | kannada | no native source found | match (mechanical) | Unpublished in Kannada. Mechanical: script block 100% pure Kannada; nasalization parity exact — candrabindu ×4, anusvara ×8. |
| hanuman-chalisa-opening | **bengali** | hanumanchalisainbengali.co.in (+ search corroboration) | match | Native Bengali Chalisa **uses candrabindu (ঁ)** — বরনউঁ, তিহুঁ, কাঁধে, মূঁজ, মোহিঁ — matching our output. Search corroborates কাঁধে মূঁজ জনেউ and যো দায়কু. (Bengali expresses 6 of the 8 master-anusvaras as homorganic nasal conjuncts — ঞ্জ/ঙ্গ/ঞ্চ — standard Bengali orthography; verified mechanically.) |
| hanuman-chalisa-opening | **gujarati** | mantramaya.com / gu.wikisource Chalisa | convention | Native Gujarati Chalisa **uses anusvara (ં)** — બરનઉં, તિહું, કાંધે, મૂંજ — vs our candrabindu (ઁ): બરનઉઁ, તિહુઁ, કાઁધે, મૂઁજ. candrabindu-vs-anusvara nasalization convention (see Outcome). |

## Roman readability pass

All 26 roman fields read. **Pure ASCII, zero diacritics, zero stray characters — RomanColloquial confirmed across the catalog.** Consistent scheme throughout: `ksh` for क्ष, `jnya` for ज्ञ, `ch` for च, plain `h` for visarga, no apostrophes, no doubled long vowels, om capitalized as `Om`.

Findings:
- **No unreadable or corrupt fields.** Short mantras (om, om-namah-shivaya, hare-krishna, gayatri, vakratunda, the Gita verses) read cleanly and idiomatically.
- **Long compound tokens are expected, not defects.** Many tokens exceed 17 chars (peak: `ashtamichandravibhrajadalikasthalashobhita`, 42, in lalita-sahasranama-opening). These are genuine single Sanskrit compounds (samasa) in the source — faithful, and acceptable because roman is a pronunciation aid shown *beside* the native script, not a standalone reading line.
- **Two mild ambiguities, both from avagraha elision** (vishnu-sahasranama-opening): `kshetrajnyokshara` (← क्षेत्रज्ञोऽक्षर) and `padmanabhomaraprabhuh` (← पद्मनाभोऽमरप्रभुः). The avagraha `ऽ`, which every Indic script in the sample *preserves* (ঽ/ઽ/ఽ), simply vanishes in RomanColloquial, fusing two words. This is inherent to the no-apostrophe scheme chosen in the frozen config (RomanColloquial over RomanReadable, per spec §3 / tools/README.md) — flagged for the human reviewer's awareness, **not** classified as an error since it is the documented, intended behavior of the locked config.

## Outcome

**15 cells: 6 match · 7 convention · 0 errors** (+ 2 "match (mechanical)" where no native source exists). No tool-output errors found; the frozen config was not touched.

Convention differences (each a valid alternative rendering, not a defect):
1. **Tamil om** — `ௐ` (ours) vs `ஓம்` (native). Pre-accepted (carry-forward c).
2. **Tamil visarga** — `꞉` U+A789 (ours) vs `:` (native). Pre-accepted (carry-forward c).
3. **Tamil avagraha** — `(அ)` parenthetical, matches native convention.
4. **anusvara vs explicit nasal/conjunct** — ours leans anusvara (ं→ ం/ಂ/ં/ং); native devotional typesetting sometimes uses an explicit halanta-nasal (`વરેણ્યમ્`) or a homorganic conjunct (`સમ્ભવો`, `স্বয়ম্ভূঃ`). Both standard.
5. **Bengali khanda-ta** — ours `ৎ` (খণ্ড-ত) vs native `ত্` (ta+virama) for pre-consonantal dental-t in `ভবৎপ্রভুঃ`. Both valid; khanda-ta is the orthographically standard Bengali form.
6. **Bengali ya** — ours `যো` vs a `য়ো` variant in some Gayatri sources. Ours is the direct Devanagari cognate.
7. **candrabindu vs anusvara nasalization (Chalisa)** — the most systematic difference, and it is **script-dependent in native practice**: Aksharamukha faithfully preserves the Devanagari candrabindu (ँ) as each script's candrabindu (Telugu U+0C00, Kannada/Bengali/Gujarati candrabindu). This **matches** native Bengali devotional convention (Bengali Chalisa sites use ঁ) and **diverges** from native Gujarati convention (Gujarati Chalisa uses anusvara ं). Telugu/Kannada have no native Awadhi-Chalisa publishing to compare against, but the candrabindu is preserved correctly and mechanically. Verdict: convention, not error — our output is the phonetically-faithful choice and is correct for at least one major script's native practice (Bengali).

**Errors: 0.** Nothing required STOP-and-report. Config remains frozen and untouched.

## Residual risk statement

This spot-check verifies the **generation config** (`generate_scripts.py` TARGETS) against how native devotional publishing renders three representative texts — establishing that our derived scripts are character-faithful to the Devanagari masters and that every divergence from native renderings is an accepted, named orthographic convention rather than a transliteration fault. It does **not** establish: (a) correctness of the underlying Devanagari master texts themselves (a content-accuracy question, not a transliteration one); (b) sandhi/segmentation or pause-marking suited to chanting; (c) coverage beyond the 3 sampled entries — the other 23 entries' derived scripts are covered only by the deterministic `--check` gate, not by native eyeballing; (d) the Awadhi-in-southern-script outputs (Telugu/Tamil/Kannada Chalisa), which are mechanically sound but have **no** native source to validate against and are inherently non-idiomatic. All of the above remain the scope of the **queued human native-script review** (per `forlater.md`), which should specifically re-examine the candrabindu-vs-anusvara Gujarati convention and the avagraha-elision roman run-ons noted here.
