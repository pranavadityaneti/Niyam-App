package com.myniyam.app.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.myniyam.app.R

/**
 * Pure-presentation countdown ring (SP-8 spec §2). Draws a track arc, a progress
 * arc that fills as the 15s elapse, and the centered seconds numeral. It owns NO
 * timer — OverlayManager drives it from the existing CountDownTimer's onTick/onFinish
 * via [setProgress]. Engine timing logic is unchanged; this view only renders state.
 */
class RingCountdownView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val strokeWidthPx = 4f * resources.displayMetrics.density

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
        strokeCap = Paint.Cap.ROUND
        color = ContextCompat.getColor(context, R.color.overlay_ring_track)
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
        strokeCap = Paint.Cap.ROUND
        color = ContextCompat.getColor(context, R.color.overlay_ring_progress)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.overlay_ink)
        textAlign = Paint.Align.CENTER
        textSize = 22f * resources.displayMetrics.scaledDensity
    }

    private val arcRect = RectF()
    /** Fraction filled, 0f (full time remaining) → 1f (elapsed). */
    private var sweepFraction = 0f
    private var secondsLeft = 0

    /** Called from OverlayManager.onTick/onFinish — no timer here. */
    fun setProgress(secondsLeftValue: Int, totalSeconds: Int) {
        secondsLeft = secondsLeftValue.coerceAtLeast(0)
        sweepFraction = if (totalSeconds <= 0) 1f
            else ((totalSeconds - secondsLeft).toFloat() / totalSeconds).coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val inset = strokeWidthPx / 2f
        arcRect.set(inset, inset, width - inset, height - inset)
        // Track (full circle) then progress arc from top, clockwise.
        canvas.drawArc(arcRect, 0f, 360f, false, trackPaint)
        canvas.drawArc(arcRect, -90f, 360f * sweepFraction, false, progressPaint)
        // Centered numeral.
        val cy = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(secondsLeft.toString(), width / 2f, cy, textPaint)
    }
}
