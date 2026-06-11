package com.myniyam.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.myniyam.app.MainActivity
import com.myniyam.app.R

/**
 * Posts the one-shot "trial ends tomorrow" reminder (SP-12 spec §2). The decision
 * is pure (TrialReminder.shouldRemind, unit-tested); only the Android post lives
 * here, mirroring CompletionNotifier's structure and permission handling.
 */
object TrialReminderNotifier {

    const val CHANNEL_ID = "niyam_trial"
    private const val NOTIFICATION_ID = 4202
    private const val TAG = "TrialReminderNotifier"

    fun registerChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Trial reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminds you the day before your free trial ends."
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    /** Builds + posts the reminder. Caller guards with TrialReminder.shouldRemind. */
    fun notifyTrialEnding(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.star_on)
            .setContentTitle(context.getString(R.string.notif_trial_title))
            .setContentText(context.getString(R.string.notif_trial_body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "notify failed (permission)", e)
        }
    }
}
