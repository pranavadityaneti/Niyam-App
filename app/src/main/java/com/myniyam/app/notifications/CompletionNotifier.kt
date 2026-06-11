package com.myniyam.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.myniyam.app.MainActivity
import com.myniyam.app.R

/**
 * Posts a one-shot "journey complete" notification (SP-7 spec §2). The
 * decision is pure (shouldPost) and unit-tested; the Android post is isolated
 * in notifyCompletion and only ever called from ProgressRepository inside a
 * try/catch, so a notification failure can never break a recorded read.
 */
object CompletionNotifier {

    const val CHANNEL_ID = "niyam_completion"
    private const val NOTIFICATION_ID = 4201
    private const val TAG = "CompletionNotifier"

    /** Pure guard: post only when the user opted in AND the OS grant is present. */
    fun shouldPost(notifyOn: Boolean, permissionGranted: Boolean): Boolean =
        notifyOn && permissionGranted

    /** True on API < 33 (no runtime grant needed) or when POST_NOTIFICATIONS is granted. */
    fun hasPostPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) true
        else ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

    fun registerChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Journey complete",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifies you when a sadhana journey reaches its goal."
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    /** Builds + posts the completion notification. Caller guards with shouldPost. */
    fun notifyCompletion(context: Context, mantraName: String) {
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
            .setContentTitle(context.getString(R.string.notif_completion_title))
            .setContentText(context.getString(R.string.notif_completion_body_fmt, mantraName))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission revoked between the guard check and the post — swallow.
            Log.w(TAG, "notify failed (permission)", e)
        }
    }
}
