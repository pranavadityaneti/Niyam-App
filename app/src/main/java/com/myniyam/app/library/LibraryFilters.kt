package com.myniyam.app.library

import com.myniyam.app.data.Deity
import com.myniyam.app.data.Intention
import com.myniyam.app.data.Mantra
import com.myniyam.app.data.SourceCategory

enum class LengthBucket {
    UNDER_30S, S30_TO_60, OVER_60S;

    companion object {
        fun of(seconds: Int): LengthBucket = when {
            seconds < 30 -> UNDER_30S
            seconds <= 60 -> S30_TO_60
            else -> OVER_60S
        }
    }
}

/** Pure library filtering (spec §4): single-select per dimension, AND across dimensions, catalog order preserved. */
object LibraryFilters {

    data class Selection(
        val category: SourceCategory? = null,
        val length: LengthBucket? = null,
        val intention: Intention? = null,
        val deity: Deity? = null
    )

    fun apply(all: List<Mantra>, sel: Selection): List<Mantra> = all.filter { m ->
        (sel.category == null || m.sourceCategory == sel.category) &&
            (sel.length == null || LengthBucket.of(m.estimatedReadSeconds) == sel.length) &&
            (sel.intention == null || sel.intention in m.intentions) &&
            (sel.deity == null || m.deity == sel.deity)
    }
}
