package com.myniyam.app.data

/**
 * The 9 user-facing language picker choices (spec §4) and the
 * (script, meaning-language) pair each resolves to. Single source of
 * truth for every screen that renders mantra content.
 */
enum class DisplayLanguage(val script: Script, val meaningLang: MeaningLang) {
    DEVANAGARI_SANSKRIT(Script.DEVANAGARI, MeaningLang.EN),
    HINDI(Script.DEVANAGARI, MeaningLang.HI),
    MARATHI(Script.DEVANAGARI, MeaningLang.MR),
    ENGLISH(Script.ROMAN, MeaningLang.EN),
    TELUGU(Script.TELUGU, MeaningLang.TE),
    TAMIL(Script.TAMIL, MeaningLang.TA),
    KANNADA(Script.KANNADA, MeaningLang.KN),
    BENGALI(Script.BENGALI, MeaningLang.BN),
    GUJARATI(Script.GUJARATI, MeaningLang.GU)
}
