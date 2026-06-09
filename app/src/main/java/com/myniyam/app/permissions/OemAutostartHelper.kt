package com.myniyam.app.permissions

import android.content.Context
import com.judemanutd.autostarter.AutoStartPermissionHelper

object OemAutostartHelper {

    enum class OemFlow {
        MIUI,
        COLOR_OS,
        FUNTOUCH_OS,
        OXYGEN_OS,
        ONE_UI,
        GENERIC
    }

    fun flowFor(manufacturer: String): OemFlow {
        return when (manufacturer.lowercase()) {
            "xiaomi", "redmi", "poco" -> OemFlow.MIUI
            "oppo", "realme" -> OemFlow.COLOR_OS
            "vivo", "iqoo" -> OemFlow.FUNTOUCH_OS
            "oneplus" -> OemFlow.OXYGEN_OS
            "samsung" -> OemFlow.ONE_UI
            else -> OemFlow.GENERIC
        }
    }

    /**
     * Best-effort deep-link to the OEM-specific autostart settings.
     * Falls back to the generic App info page on any failure.
     */
    fun openAutostartSettings(ctx: Context) {
        runCatching {
            AutoStartPermissionHelper.getInstance().getAutoStartPermission(ctx, true, true)
        }
    }
}
