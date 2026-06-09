package com.myniyam.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.myniyam.app.MainActivity
import com.myniyam.app.NiyamApplication
import com.myniyam.app.R

class AppLockForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> { /* onCreate already started foreground */ }
            ACTION_BLOCKED_APP_FOREGROUND -> {
                val pkg = intent.getStringExtra(EXTRA_PACKAGE) ?: return START_STICKY
                // TODO(Task 19): OverlayManager.show(applicationContext, pkg)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        // TODO(Task 19): OverlayManager.hide(applicationContext)
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val launch = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            this, 0, launch,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NiyamApplication.CHANNEL_ID_FOREGROUND)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notif_text))
            .setContentIntent(pi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val NOTIF_ID = 1001

        const val ACTION_START = "com.myniyam.app.action.START"
        const val ACTION_BLOCKED_APP_FOREGROUND = "com.myniyam.app.action.BLOCKED_APP_FOREGROUND"
        const val EXTRA_PACKAGE = "extra_package"
    }
}
