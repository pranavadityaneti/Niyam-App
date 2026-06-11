package com.myniyam.app.overlay

import android.content.Context
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.myniyam.app.R
import com.myniyam.app.data.CurrentSadhana
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.Script
import com.myniyam.app.data.ThemePref
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.progress.ProgressRepository
import com.myniyam.app.service.AppLockAccessibilityService
import com.myniyam.app.service.UnlockGrace

object OverlayManager {

    // The timer is the engine's rule, not the content's — spec keeps it fixed at 15s.
    private const val UNLOCK_TIMER_SECONDS = 15

    private var overlayView: View? = null
    private var attachedPkg: String? = null
    private var timer: CountDownTimer? = null

    fun show(rawCtx: Context, pkg: String) {
        if (overlayView != null) return  // already showing — no-op

        // Chrome strings (overline, Continue) follow the in-app language (SP-11) —
        // purely a resource-resolution wrapper, no flow change.
        val ctx = com.myniyam.app.ui.LocaleBridge.wrap(rawCtx)

        val view = LayoutInflater.from(ctx).inflate(R.layout.overlay_mantra, null, false)

        MantraRepository.ensureLoaded(ctx)
        val mantra = MantraRepository.displayMantra(CurrentSadhana.MANTRA_ID)
        val lang = CurrentSadhana.LANGUAGE
        view.findViewById<TextView>(R.id.overlay_devanagari).text = mantra.text.forScript(lang.script)
        // Roman script already IS the transliteration — don't render the same line twice (forlater 6).
        view.findViewById<TextView>(R.id.overlay_transliteration).apply {
            if (lang.script == Script.ROMAN) {
                visibility = View.GONE
            } else {
                text = mantra.text.forScript(Script.ROMAN)
            }
        }
        view.findViewById<TextView>(R.id.overlay_meaning).text = mantra.meaning.forLang(lang.meaningLang)
        view.findViewById<TextView>(R.id.overlay_label).text =
            ctx.getString(R.string.overlay_label_fmt, mantra.canonicalName)

        applyTheme(ctx, view)

        val ring = view.findViewById<RingCountdownView>(R.id.overlay_ring)
        val continueBtn = view.findViewById<Button>(R.id.overlay_continue)

        ring.setProgress(UNLOCK_TIMER_SECONDS, UNLOCK_TIMER_SECONDS)
        continueBtn.isEnabled = false
        continueBtn.setOnClickListener {
            ProgressRepository.recordRead(ctx, mantra.id)
            // A completed read earns this package its grace window (hide-on-leave does not).
            UnlockGrace.grant(pkg, SystemClock.elapsedRealtime())
            hide(ctx)
        }

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
            startTimer(ring, continueBtn)
        } catch (e: Exception) {
            android.util.Log.e("OverlayManager", "Failed to attach overlay", e)
        }
    }

    /**
     * Dark-variant binding (forlater 5) — purely visual, applied once at inflate.
     * Follows the user's themePref; SYSTEM resolves via the device uiMode.
     */
    private fun applyTheme(ctx: Context, view: View) {
        val dark = when (UserPrefs.snapshot().themePref) {
            ThemePref.DARK -> true
            ThemePref.LIGHT -> false
            ThemePref.SYSTEM ->
                (ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES
        }
        if (!dark) return  // light = the existing XML defaults, untouched

        fun color(id: Int) = ContextCompat.getColor(ctx, id)
        view.setBackgroundResource(R.drawable.bg_overlay_gradient_dark)
        view.findViewById<TextView>(R.id.overlay_label).setTextColor(color(R.color.overlay_label_warm_dark))
        view.findViewById<TextView>(R.id.overlay_devanagari).setTextColor(color(R.color.overlay_ink_dark))
        view.findViewById<TextView>(R.id.overlay_transliteration).setTextColor(color(R.color.overlay_roman_dark))
        view.findViewById<TextView>(R.id.overlay_meaning).setTextColor(color(R.color.overlay_ink_muted_dark))
        view.findViewById<RingCountdownView>(R.id.overlay_ring)
            .setPalette(color(R.color.overlay_ring_track_dark), color(R.color.overlay_ink_dark))
    }

    fun isShowing(): Boolean = overlayView != null

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

    private fun startTimer(ring: RingCountdownView, continueBtn: Button) {
        val totalMs = UNLOCK_TIMER_SECONDS * 1000L
        timer = object : CountDownTimer(totalMs, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000L).toInt().coerceAtLeast(0)
                ring.setProgress(secondsLeft, UNLOCK_TIMER_SECONDS)
            }

            override fun onFinish() {
                ring.setProgress(0, UNLOCK_TIMER_SECONDS)
                continueBtn.isEnabled = true
            }
        }.start()
    }
}
