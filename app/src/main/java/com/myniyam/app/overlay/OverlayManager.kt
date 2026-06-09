package com.myniyam.app.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.myniyam.app.R
import com.myniyam.app.data.PlaceholderMantra
import com.myniyam.app.service.AppLockAccessibilityService

object OverlayManager {

    private var overlayView: View? = null
    private var attachedPkg: String? = null
    private var timer: CountDownTimer? = null

    fun show(ctx: Context, pkg: String) {
        if (overlayView != null) return  // already showing — no-op

        val view = LayoutInflater.from(ctx).inflate(R.layout.overlay_mantra, null, false)

        view.findViewById<TextView>(R.id.overlay_devanagari).text = PlaceholderMantra.DEVANAGARI
        view.findViewById<TextView>(R.id.overlay_transliteration).text = PlaceholderMantra.TRANSLITERATION
        view.findViewById<TextView>(R.id.overlay_meaning).text = PlaceholderMantra.ENGLISH_MEANING

        val countdown = view.findViewById<TextView>(R.id.overlay_countdown)
        val continueBtn = view.findViewById<Button>(R.id.overlay_continue)

        countdown.text = ctx.getString(R.string.overlay_unlocking_in, PlaceholderMantra.UNLOCK_TIMER_SECONDS)
        continueBtn.isEnabled = false
        continueBtn.setOnClickListener { hide(ctx) }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        try {
            wm.addView(view, params)
            overlayView = view
            attachedPkg = pkg
            startTimer(ctx, countdown, continueBtn)
        } catch (e: Exception) {
            android.util.Log.e("OverlayManager", "Failed to attach overlay", e)
        }
    }

    fun hide(ctx: Context) {
        timer?.cancel()
        timer = null

        val view = overlayView ?: return
        val pkg = attachedPkg

        try {
            val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.removeView(view)
        } catch (e: Exception) {
            android.util.Log.e("OverlayManager", "Failed to remove overlay", e)
        }

        overlayView = null
        attachedPkg = null

        // Tell the AccessibilityService to debounce re-triggers for this package.
        if (pkg != null) AppLockAccessibilityService.get()?.markDismissed(pkg)
    }

    private fun startTimer(ctx: Context, countdown: TextView, continueBtn: Button) {
        val totalMs = PlaceholderMantra.UNLOCK_TIMER_SECONDS * 1000L
        timer = object : CountDownTimer(totalMs, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000L).toInt().coerceAtLeast(0)
                countdown.text = ctx.getString(R.string.overlay_unlocking_in, secondsLeft)
            }

            override fun onFinish() {
                countdown.text = ctx.getString(R.string.overlay_unlocking_in, 0)
                continueBtn.isEnabled = true
            }
        }.start()
    }
}
