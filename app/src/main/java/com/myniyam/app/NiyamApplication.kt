package com.myniyam.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.UserPrefs
import com.google.android.gms.ads.MobileAds
import com.myniyam.app.notifications.CompletionNotifier
import com.myniyam.app.progress.ProgressRepository

class NiyamApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        registerForegroundServiceChannel()
        CompletionNotifier.registerChannel(this)
        Thread {
            MantraRepository.ensureLoaded(this)
            UserPrefs.ensureLoaded(this)
            ProgressRepository.warmUp(this)
            MobileAds.initialize(this)
        }.start()
    }

    private fun registerForegroundServiceChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_FOREGROUND,
            "Niyam protection",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Persistent notification shown while Niyam is guarding blocked apps."
            setShowBadge(false)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID_FOREGROUND = "niyam_foreground_service"
    }
}
