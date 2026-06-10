package com.myniyam.app.progress

import com.myniyam.app.data.Intention
import com.myniyam.app.data.Mantra
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.StarterMantras

/**
 * Next-sadhana recommendations (spec §4): the user's intention priority
 * list minus completed/current, backfilled from the rest of the catalog
 * in catalog order. Always ≤3.
 */
object NextSadhana {

    fun candidates(intention: Intention, completed: Set<String>, currentId: String): List<Mantra> {
        val excluded = completed + currentId
        val fromIntention = StarterMantras.priorityIds(intention)
            .filterNot { it in excluded }
            .mapNotNull { MantraRepository.byId(it) }
        if (fromIntention.size >= 3) return fromIntention.take(3)
        val backfill = MantraRepository.all()
            .filterNot { it.id in excluded || fromIntention.any { m -> m.id == it.id } }
        return (fromIntention + backfill).take(3)
    }
}
