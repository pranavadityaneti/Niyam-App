package com.myniyam.app.data

/**
 * Deterministic per-intention starter recommendations (SP-3 spec §6) —
 * the brief's own groupings as ordered priority lists; the picker shows
 * the first 3 present in the catalog. Also seeds SP-4's next-sadhana logic.
 */
object StarterMantras {

    private val PRIORITY: Map<Intention, List<String>> = mapOf(
        Intention.FOCUS to listOf("gita-2-47", "gita-6-5", "gita-2-14", "asato-ma", "gita-6-6"),
        Intention.CALM to listOf("mahamrityunjaya", "om-sahanavavatu", "om-namah-shivaya", "gita-2-70", "twameva-mata"),
        Intention.SADHANA to listOf("gayatri", "vakratunda", "saraswati-vandana", "guru-brahma", "hare-krishna"),
        Intention.DHARMA to listOf("gita-4-7-8", "gita-18-66", "gita-3-35", "purusha-suktam", "nasadiya-suktam"),
        Intention.DEVOTION to listOf(
            "hanuman-chalisa-opening", "vishnu-sahasranama-opening",
            "lalita-sahasranama-opening", "krishna-ashtakam", "ram-raksha-opening"
        )
    )

    fun forIntention(intention: Intention): List<Mantra> =
        PRIORITY.getValue(intention).mapNotNull { MantraRepository.byId(it) }.take(3)

    /** Full 5-id priority list for an intention (SP-4 next-sadhana seeding). */
    fun priorityIds(intention: Intention): List<String> = PRIORITY.getValue(intention)
}
