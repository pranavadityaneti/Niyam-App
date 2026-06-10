package com.myniyam.app.progress

/** Pure date math over epochDay longs (spec §4). All edges JVM-tested. */
object ProgressMath {

    /**
     * Consecutive days ending today — or ending yesterday when today has
     * no read yet (the streak isn't broken until the day is over).
     */
    fun streak(readDays: Set<Long>, today: Long): Int {
        val anchor = when {
            today in readDays -> today
            (today - 1) in readDays -> today - 1
            else -> return 0
        }
        var n = 0
        while (anchor - n in readDays) n++
        return n
    }

    fun dayN(distinctDays: Int, capM: Int): Int = distinctDays.coerceAtMost(capM)

    /** Completion predicate (spec §2): the journey completes when counted days reach the threshold. */
    fun isComplete(distinctDays: Int, threshold: Int): Boolean = distinctDays >= threshold
}
