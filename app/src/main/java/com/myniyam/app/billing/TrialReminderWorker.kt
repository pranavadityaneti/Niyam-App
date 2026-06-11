package com.myniyam.app.billing

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.notifications.CompletionNotifier
import com.myniyam.app.notifications.TrialReminderNotifier
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * Daily check for the day-6 trial reminder (SP-12 spec §2). WorkManager survives
 * reboots and runs without the app being opened; the pure decision lives in
 * TrialReminder and the persisted trialReminderShown flag prevents repeats.
 */
class TrialReminderWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        UserPrefs.ensureLoaded(applicationContext)
        val snap = UserPrefs.snapshot()
        val today = LocalDate.now().toEpochDay()
        val remind = TrialReminder.shouldRemind(
            premiumActive = snap.premiumActive,
            trialStartEpochDay = snap.trialStartEpochDay,
            todayEpochDay = today,
            alreadyShown = snap.trialReminderShown
        )
        if (remind && CompletionNotifier.hasPostPermission(applicationContext)) {
            TrialReminderNotifier.notifyTrialEnding(applicationContext)
            runBlocking { UserPrefs.setTrialReminderShown(applicationContext) }
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "niyam_trial_reminder"

        /** Idempotent daily schedule — call at Application start. */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<TrialReminderWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
