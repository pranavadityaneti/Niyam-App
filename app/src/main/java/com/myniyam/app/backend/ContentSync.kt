package com.myniyam.app.backend

import android.content.Context
import android.util.Log
import com.myniyam.app.data.MantraRepository
import io.github.jan.supabase.storage.storage
import java.io.File

/**
 * OTA mantra-content sync (SP-P-OTA, task 6). When the server's content_version
 * is newer than what's cached locally, downloads `content/mantras.json` from the
 * public Storage bucket, validates it, swaps it into [MantraRepository], and
 * caches it. Offline-first: the bundled asset remains the baseline and is used
 * whenever no valid remote cache exists or a download is invalid.
 */
object ContentSync {

    private const val TAG = "ContentSync"
    private const val CONTENT_FILE = "mantras.json"          // filesDir cache
    private const val VERSION_FILE = "content_version"
    private const val BUCKET = "content"
    private const val OBJECT = "mantras.json"

    /** The locally-cached remote catalog JSON, or null if none/unreadable. */
    fun cachedJsonOrNull(context: Context): String? {
        val f = File(context.filesDir, CONTENT_FILE)
        return if (f.exists()) try { f.readText() } catch (e: Exception) { null } else null
    }

    private fun cachedVersion(context: Context): Int =
        try { File(context.filesDir, VERSION_FILE).readText().trim().toInt() } catch (e: Exception) { 0 }

    /** Pull a newer catalog if the server has one. Best-effort; never throws. */
    suspend fun maybeUpdate(context: Context, remoteVersion: Int) {
        if (remoteVersion <= cachedVersion(context)) return
        try {
            val bytes = SupabaseClientProvider.client.storage.from(BUCKET).downloadPublic(OBJECT)
            val text = bytes.decodeToString()
            // Validate by parsing into the repository before persisting.
            if (MantraRepository.initFromJson(text)) {
                File(context.filesDir, CONTENT_FILE).writeText(text)
                File(context.filesDir, VERSION_FILE).writeText(remoteVersion.toString())
                Log.i(TAG, "content updated to v$remoteVersion")
            } else {
                Log.w(TAG, "remote content failed validation; keeping current")
            }
        } catch (e: Exception) {
            Log.w(TAG, "content download failed; keeping current", e)
        }
    }
}
