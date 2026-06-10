package com.myniyam.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MantraCatalog(
    val schemaVersion: Int,
    val contentVersion: String,
    val mantras: List<Mantra>
)

@Serializable
data class Mantra(
    val id: String,
    val canonicalName: String,
    val originalLanguage: OriginalLanguage,
    val text: MantraText,
    val meaning: MantraMeaning,
    val source: String,
    val sourceRefs: List<String>,
    val deity: Deity,
    val intentions: List<Intention>,
    val estimatedReadSeconds: Int,
    val completionThresholdDays: Int
)

@Serializable
data class MantraText(
    val devanagari: String,
    val telugu: String,
    val tamil: String,
    val kannada: String,
    val bengali: String,
    val gujarati: String,
    val roman: String
) {
    fun forScript(script: Script): String = when (script) {
        Script.DEVANAGARI -> devanagari
        Script.TELUGU -> telugu
        Script.TAMIL -> tamil
        Script.KANNADA -> kannada
        Script.BENGALI -> bengali
        Script.GUJARATI -> gujarati
        Script.ROMAN -> roman
    }
}

@Serializable
data class MantraMeaning(
    val en: String,
    val hi: String,
    val te: String,
    val ta: String,
    val kn: String,
    val mr: String,
    val bn: String,
    val gu: String
) {
    fun forLang(lang: MeaningLang): String = when (lang) {
        MeaningLang.EN -> en
        MeaningLang.HI -> hi
        MeaningLang.TE -> te
        MeaningLang.TA -> ta
        MeaningLang.KN -> kn
        MeaningLang.MR -> mr
        MeaningLang.BN -> bn
        MeaningLang.GU -> gu
    }
}

@Serializable
enum class OriginalLanguage {
    @SerialName("sanskrit") SANSKRIT,
    @SerialName("awadhi") AWADHI
}

@Serializable
enum class Deity {
    @SerialName("shiva") SHIVA,
    @SerialName("vishnu") VISHNU,
    @SerialName("devi") DEVI,
    @SerialName("ganesha") GANESHA,
    @SerialName("hanuman") HANUMAN,
    @SerialName("krishna") KRISHNA,
    @SerialName("rama") RAMA,
    @SerialName("saraswati") SARASWATI,
    @SerialName("lakshmi") LAKSHMI,
    @SerialName("universal") UNIVERSAL
}

@Serializable
enum class Intention {
    @SerialName("focus") FOCUS,
    @SerialName("calm") CALM,
    @SerialName("sadhana") SADHANA,
    @SerialName("dharma") DHARMA,
    @SerialName("devotion") DEVOTION
}

enum class Script { DEVANAGARI, TELUGU, TAMIL, KANNADA, BENGALI, GUJARATI, ROMAN }

enum class MeaningLang { EN, HI, TE, TA, KN, MR, BN, GU }
