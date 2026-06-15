package com.myniyam.app.progress

import android.content.Context
import android.util.Log
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.notifications.CompletionNotifier
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.concurrent.Executors

/**
 * Read-event recording + home stats (spec §4). recordRead is
 * fire-and-forget on a single-thread executor — the unlock path can
 * never block or crash on it.
 */
object ProgressRepository {

    private const val TAG = "ProgressRepository"
    private val executor = Executors.newSingleThreadExecutor()

    fun warmUp(context: Context) {
        val app = context.applicationContext
        executor.execute {
            try {
                NiyamDatabase.get(app)
            } catch (e: Exception) {
                Log.e(TAG, "warmUp failed", e)
            }
        }
    }

    fun recordRead(context: Context, mantraId: String) {
        val app = context.applicationContext
        executor.execute {
            try {
                val today = LocalDate.now().toEpochDay()
                runBlocking {
                    NiyamDatabase.get(app).readEventDao().insert(
                        ReadEventEntity(
                            mantraId = mantraId,
                            epochDay = today,
                            timestampMs = System.currentTimeMillis()
                        )
                    )
                    maybeComplete(app, mantraId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "recordRead failed", e)
            }
        }
    }

    private suspend fun maybeComplete(context: Context, mantraId: String) {
        val snap = UserPrefs.snapshot()
        if (mantraId != snap.currentMantraId) return
        if (mantraId in snap.completedMantraIds) return
        val mantra = MantraRepository.byId(mantraId) ?: return
        val days = NiyamDatabase.get(context).readEventDao()
            .distinctDaysFor(mantraId, snap.sadhanaStartEpochDay)
        if (ProgressMath.isComplete(days, mantra.completionThresholdDays)) {
            UserPrefs.markCompleted(context, mantraId)
            try {
                val notifyOn = UserPrefs.snapshot().notifyOnCompletion
                if (CompletionNotifier.shouldPost(notifyOn, CompletionNotifier.hasPostPermission(context))) {
                    CompletionNotifier.notifyCompletion(
                        context,
                        MantraRepository.displayMantra(mantraId).name.forScript(snap.displayLanguage.script)
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "completion notification failed", e)
            }
        }
    }

    data class HomeStats(val dayN: Int, val dayM: Int, val streak: Int, val todayReads: Int)

    suspend fun homeStats(context: Context): HomeStats {
        return try {
            val app = context.applicationContext
            val snap = UserPrefs.snapshot()
            val dao = NiyamDatabase.get(app).readEventDao()
            val today = LocalDate.now().toEpochDay()
            val mantra = MantraRepository.displayMantra(snap.currentMantraId)
            val distinct = dao.distinctDaysFor(snap.currentMantraId, snap.sadhanaStartEpochDay)
            HomeStats(
                dayN = ProgressMath.dayN(distinct, mantra.completionThresholdDays),
                dayM = mantra.completionThresholdDays,
                streak = ProgressMath.streak(dao.allReadDays().toSet(), today),
                todayReads = dao.countOn(today)
            )
        } catch (e: Exception) {
            Log.e(TAG, "homeStats failed", e)
            HomeStats(0, 14, 0, 0)
        }
    }
}
