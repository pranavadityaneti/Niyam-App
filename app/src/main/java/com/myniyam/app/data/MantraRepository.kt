package com.myniyam.app.data

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json

/**
 * Parse-once, in-memory, immutable content store (spec §7).
 * Never throws to callers; on any failure serves the built-in om FALLBACK.
 */
object MantraRepository {

    private const val TAG = "MantraRepository"
    private const val ASSET_PATH = "content/mantras.json"
    private const val SUPPORTED_SCHEMA_VERSION = 1

    @Volatile private var catalog: MantraCatalog? = null
    @Volatile private var loadAttempted = false

    private val json = Json { ignoreUnknownKeys = true }

    /** Built-in emergency entry — the overlay can never crash from bad content (spec §8). */
    val FALLBACK = Mantra(
        id = "om",
        canonicalName = "Om (Pranava)",
        name = MantraText(
            devanagari = "ॐ", telugu = "ఓం", tamil = "ௐ",
            kannada = "ಓಂ", bengali = "ওঁ", gujarati = "ૐ", roman = "Om (Pranava)"
        ),
        originalLanguage = OriginalLanguage.SANSKRIT,
        text = MantraText(
            devanagari = "ॐ", telugu = "ఓం", tamil = "ௐ",
            kannada = "ಓಂ", bengali = "ওঁ", gujarati = "ૐ", roman = "Om"
        ),
        meaning = MantraMeaning(
            en = "The primordial sound of the universe. One syllable holding creation, " +
                "preservation, and dissolution — a single breath that settles the mind.",
            hi = "ब्रह्मांड की आदि ध्वनि।",
            te = "విశ్వపు ఆది నాదం.",
            ta = "பிரபஞ்சத்தின் ஆதி ஒலி.",
            kn = "ವಿಶ್ವದ ಆದಿ ನಾದ.",
            mr = "विश्वाचा आदिनाद.",
            bn = "বিশ্বের আদি ধ্বনি।",
            gu = "બ્રહ્માંડનો આદિ નાદ."
        ),
        source = "Mandukya Upanishad",
        sourceLabel = MantraText(
            devanagari = "माण्डूक्य उपनिषद्", telugu = "మాండూక్య ఉపనిషద్", tamil = "மாண்டூக்ய உபநிஷத்",
            kannada = "ಮಾಂಡೂಕ್ಯ ಉಪನಿಷದ್", bengali = "মাণ্ডূক্য উপনিষদ্", gujarati = "માણ્ડૂક્ય ઉપનિષદ્",
            roman = "Mandukya Upanishad"
        ),
        sourceRefs = listOf("https://sanskritdocuments.org/doc_upanishhat/mandukya.html"),
        deity = Deity.UNIVERSAL,
        intentions = listOf(Intention.CALM, Intention.SADHANA),
        estimatedReadSeconds = 5,
        completionThresholdDays = 14
    )

    /** Pure-JVM parsing core. Returns false (and clears state) on any failure. */
    fun initFromJson(jsonText: String): Boolean {
        return try {
            val parsed = json.decodeFromString<MantraCatalog>(jsonText)
            if (parsed.schemaVersion != SUPPORTED_SCHEMA_VERSION) {
                Log.e(TAG, "Unsupported schemaVersion ${parsed.schemaVersion}")
                catalog = null
                false
            } else {
                catalog = parsed
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse mantra catalog", e)
            catalog = null
            false
        }
    }

    /**
     * Android entry point: loads the catalog once. Prefers the OTA-cached remote
     * file in filesDir (written by ContentSync); falls back to the bundled asset
     * if there's no cache or the cache fails to parse. Safe to call repeatedly.
     */
    fun ensureLoaded(context: Context) {
        if (catalog != null || loadAttempted) return
        synchronized(this) {
            if (catalog != null || loadAttempted) return
            loadAttempted = true
            // 1. OTA cache (offline-first override).
            val cached = try {
                val f = java.io.File(context.filesDir, "mantras.json")
                if (f.exists()) f.readText() else null
            } catch (e: Exception) { null }
            if (cached != null && initFromJson(cached)) return
            // 2. Bundled asset (the always-present baseline).
            val text = try {
                context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read $ASSET_PATH", e)
                return
            }
            initFromJson(text)
        }
    }

    fun all(): List<Mantra> = catalog?.mantras ?: emptyList()

    fun byId(id: String): Mantra? = catalog?.mantras?.firstOrNull { it.id == id }

    /** Overlay-facing accessor: always returns a renderable entry. */
    fun displayMantra(id: String): Mantra = byId(id) ?: FALLBACK

    /** Test hook — clears parsed state between unit tests. */
    fun resetForTest() {
        catalog = null
        loadAttempted = false
    }
}
