package com.myniyam.app.backend

import android.content.Context
import android.util.Log
import com.myniyam.app.billing.Entitlements
import com.myniyam.app.data.AppCatalog
import com.myniyam.app.data.CatalogApp
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

/**
 * OTA remote config + content (SP-P-OTA). Pulls the public `app_config` rows at
 * launch so content/behaviour can change WITHOUT shipping a build. Offline-first:
 * the last fetch is cached to a local file and read synchronously on cold start;
 * every accessor falls back to the value bundled in THIS build, so a missing row
 * or no network can never brick the app. Never on the blocking-engine path.
 */
object RemoteConfig {

    private const val TAG = "RemoteConfig"
    private const val CACHE_FILE = "remote_config.json"

    private val json = Json { ignoreUnknownKeys = true }
    @Volatile private var cache: JsonObject = JsonObject(emptyMap())

    @Serializable
    private data class ConfigRow(val key: String, val value: JsonElement)

    /** Synchronous: load the last cached config so values are ready offline. */
    fun ensureLoaded(context: Context) {
        try {
            val f = File(context.filesDir, CACHE_FILE)
            if (f.exists()) {
                cache = json.parseToJsonElement(f.readText()).jsonObject
                apply()
            }
        } catch (e: Exception) {
            Log.w(TAG, "cache load failed; using build defaults", e)
        }
    }

    /** Best-effort network refresh. Caches the result and re-applies. No-op on failure. */
    suspend fun refresh(context: Context) {
        try {
            val rows = SupabaseClientProvider.client.from("app_config")
                .select().decodeList<ConfigRow>()
            cache = JsonObject(rows.associate { it.key to it.value })
            File(context.filesDir, CACHE_FILE).writeText(json.encodeToString(JsonObject.serializer(), cache))
            apply()
            ContentSync.maybeUpdate(context, contentVersion())  // P-OTA task 6
        } catch (e: Exception) {
            Log.w(TAG, "refresh failed; keeping cached/build values", e)
        }
    }

    /** Push remote lists into the static holders the rest of the app reads (task 3). */
    private fun apply() {
        Entitlements.activeFreeMantraIds = freeMantraIds()
        AppCatalog.APPS = blockableApps()
    }

    // --- accessors (cache → build-default) -----------------------------------

    private fun el(key: String): JsonElement? = cache[key]

    fun freeMantraIds(): Set<String> =
        (el("free_mantra_ids") as? JsonArray)
            ?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }?.toSet()
            ?.takeIf { it.isNotEmpty() } ?: Entitlements.FREE_MANTRA_IDS

    fun blockableApps(): List<CatalogApp> =
        (el("blockable_apps") as? JsonArray)?.mapNotNull { row ->
            val o = row as? JsonObject ?: return@mapNotNull null
            val n = o["name"]?.jsonPrimitive?.contentOrNull
            val p = o["pkg"]?.jsonPrimitive?.contentOrNull
            val s = o["slug"]?.jsonPrimitive?.contentOrNull ?: ""
            if (n != null && p != null) CatalogApp(n, p, s) else null
        }?.takeIf { it.isNotEmpty() } ?: AppCatalog.DEFAULT_APPS

    private fun paywall(): JsonObject? = el("paywall") as? JsonObject
    fun paywallPrice(field: String, default: Int): Int =
        (paywall()?.get(field) as? JsonPrimitive)?.intOrNull ?: default
    fun paywallTrust(): String? =
        (paywall()?.get("trust") as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }

    fun flag(key: String, default: Boolean): Boolean =
        ((el("feature_flags") as? JsonObject)?.get(key) as? JsonPrimitive)?.booleanOrNull ?: default

    fun minSupportedVersionCode(): Int =
        (el("min_supported_version_code") as? JsonPrimitive)?.intOrNull ?: 0

    fun updateMessage(): String? =
        (el("update_message") as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }

    fun contentVersion(): Int = (el("content_version") as? JsonPrimitive)?.intOrNull ?: 0

    data class Announcement(val title: String, val body: String, val key: String)
    private fun announcement(): Announcement? {
        val o = el("announcement") as? JsonObject ?: return null
        val active = (o["active"] as? JsonPrimitive)?.booleanOrNull ?: false
        if (!active) return null
        val title = (o["title"] as? JsonPrimitive)?.contentOrNull ?: ""
        val body = (o["body"] as? JsonPrimitive)?.contentOrNull ?: ""
        if (title.isBlank() && body.isBlank()) return null
        // Dismissal key: a stable `id` from config if provided, else a hash of the
        // content. Never the bare title (which can be blank or reused), so EVERY
        // announcement is dismissible exactly once.
        val id = (o["id"] as? JsonPrimitive)?.contentOrNull
        val key = id?.takeIf { it.isNotBlank() } ?: "$title|$body".hashCode().toString()
        return Announcement(title, body, key)
    }

    private const val DISMISS_FILE = "announcement_dismissed"

    /** The active announcement, unless this device already dismissed this exact one. */
    fun activeAnnouncement(context: Context): Announcement? {
        val a = announcement() ?: return null
        val dismissed = try { File(context.filesDir, DISMISS_FILE).readText() } catch (e: Exception) { "" }
        return if (a.key == dismissed) null else a
    }

    fun dismissAnnouncement(context: Context, key: String) {
        try { File(context.filesDir, DISMISS_FILE).writeText(key) } catch (e: Exception) { }
    }
}
