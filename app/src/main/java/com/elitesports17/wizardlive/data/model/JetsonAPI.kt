package com.elitesports17.wizardlive.data.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class JetsonStatus(
    val battery: String = "",
    val cpuTemp: String = "",
    val status: String = "",
    val wizardName: String = "",
    val fullSerialNumber: String = ""
)

data class WizardInfo(
    val ok: Boolean,
    val username: String = ""
)

class JetsonApi(
    private val apiHost: String = "10.42.0.1",
    private val apiPort: Int = 2223,
    private val streamPort: Int = 8889
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()
    private fun emptyJsonBody() = "{}".toRequestBody(jsonMedia)

    private val baseUrl get() = "http://$apiHost:$apiPort"
    private val streamBaseUrl get() = "http://$apiHost:$streamPort"

    fun streamCandidates(): List<String> {
        val base = "$streamBaseUrl/live/mystream/"
        return listOf(
            base,
            base + "index.m3u8",
            base + "playlist.m3u8",
            base.removeSuffix("/") + ".m3u8"
        ).distinct()
    }

    private val statusUrl get() = "$baseUrl/status"
    private val wizardUrl get() = "$baseUrl/get_wizard_id"

    private val previewStatusUrl get() = "$baseUrl/preview_status"
    private val startPreviewUrl get() = "$baseUrl/start_preview"
    private val togglePreviewUrl get() = "$baseUrl/toggle_preview"

    private val startCamsUrl get() = "$baseUrl/start_cams"
    private val camsStatusUrl get() = "$baseUrl/cams_status"

    private val startStreamWizardUrl get() = "$baseUrl/start_streaming_full?type=wizard"
    private val stopStreamUrl get() = "$baseUrl/stop_streaming_full"

    private fun parseOrThrow(code: Int, body: String, endpoint: String): JSONObject {
        if (code !in 200..299) throw RuntimeException("HTTP $code en $endpoint: $body")

        val json = runCatching { JSONObject(body) }.getOrNull()
        if (json != null && json.optBoolean("error", false)) {
            val msg = json.optString("message", "error")
            throw RuntimeException("$endpoint: $msg")
        }
        return json ?: JSONObject()
    }

    suspend fun getStatus(): JetsonStatus = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(statusUrl).get().build()
        client.newCall(req).execute().use { res ->
            val body = res.body?.string().orEmpty()
            val j = parseOrThrow(res.code, body, "/status")

            JetsonStatus(
                battery = j.optString("battery", ""),
                cpuTemp = j.optString("cpu_temp", ""),
                status = j.optString("status", ""),
                wizardName = j.optString("wizard_name", ""),
                fullSerialNumber = j.optString("full_serial_number", "")
            )
        }
    }

    suspend fun getWizardInfo(): WizardInfo = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(wizardUrl).get().build()
        client.newCall(req).execute().use { res ->
            val body = res.body?.string().orEmpty()
            if (res.code !in 200..299) return@withContext WizardInfo(ok = false)

            val j = runCatching { JSONObject(body) }.getOrNull() ?: return@withContext WizardInfo(ok = false)
            val msg = j.optString("message", "")
            val wizardId = j.optString("wizard_id", "")
            val username = j.optString("username", "")

            val ok = (msg == "Current Wizard ID and username" && wizardId.isNotBlank())
            WizardInfo(ok = ok, username = if (ok) username else "")
        }
    }

    data class CamsInfo(val running: Boolean, val message: String = "")

    private fun guessCamsRunning(j: JSONObject): Boolean {
        val msg = j.optString("message", "").lowercase()
        return when {
            msg.contains("cams are running") -> true
            msg.contains("are running") -> true
            msg.contains("running") && !msg.contains("not") -> true
            msg.contains("started") -> true
            msg.contains("not running") -> false
            msg.contains("stopped") -> false
            else -> false
        }
    }

    suspend fun getCamsStatus(): CamsInfo = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(camsStatusUrl).get().build()
        client.newCall(req).execute().use { res ->
            val body = res.body?.string().orEmpty()
            if (res.code !in 200..299) return@withContext CamsInfo(running = false, message = body)

            val j = runCatching { JSONObject(body) }.getOrNull() ?: JSONObject()
            CamsInfo(running = guessCamsRunning(j), message = j.optString("message", ""))
        }
    }

    suspend fun startCams(): Boolean = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(startCamsUrl).post(emptyJsonBody()).build()
        client.newCall(req).execute().use { res ->
            val body = res.body?.string().orEmpty()
            parseOrThrow(res.code, body, "/start_cams")
            true
        }
    }

    data class PreviewInfo(val active: Boolean, val message: String = "")

    private fun guessPreviewActive(j: JSONObject): Boolean {
        val msg = j.optString("message", "").lowercase()
        return when {
            msg.contains("preview is active") -> true
            msg.contains("preview active") -> true
            msg.contains("active") && !msg.contains("not") -> true
            msg.contains("running") && !msg.contains("not") -> true
            msg.contains("not active") -> false
            msg.contains("inactive") -> false
            else -> false
        }
    }

    suspend fun getPreviewStatus(): PreviewInfo = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(previewStatusUrl).get().build()
        client.newCall(req).execute().use { res ->
            val body = res.body?.string().orEmpty()
            if (res.code !in 200..299) return@withContext PreviewInfo(active = false, message = body)

            val j = runCatching { JSONObject(body) }.getOrNull() ?: JSONObject()
            PreviewInfo(active = guessPreviewActive(j), message = j.optString("message", ""))
        }
    }

    suspend fun startPreview(): Boolean = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(startPreviewUrl).post(emptyJsonBody()).build()
        client.newCall(req).execute().use { res ->
            val body = res.body?.string().orEmpty()
            parseOrThrow(res.code, body, "/start_preview")
            true
        }
    }

    suspend fun togglePreview(): Boolean = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(togglePreviewUrl).post(emptyJsonBody()).build()
        client.newCall(req).execute().use { res ->
            val body = res.body?.string().orEmpty()
            parseOrThrow(res.code, body, "/toggle_preview")
            true
        }
    }

    suspend fun ensurePreviewActive(): Boolean = withContext(Dispatchers.IO) {
        var cams = getCamsStatus()
        if (!cams.running) {
            startCams()
            repeat(8) {
                delay(400)
                cams = getCamsStatus()
                if (cams.running) return@repeat
            }
            if (!cams.running) return@withContext false
        }

        var pv = getPreviewStatus()
        if (pv.active) return@withContext true

        val started = runCatching { startPreview() }.getOrElse { false }
        if (!started) runCatching { togglePreview() }

        repeat(8) {
            delay(350)
            pv = getPreviewStatus()
            if (pv.active) return@withContext true
        }
        false
    }

    // ✅ NUEVO: apagar preview de verdad (backend)
    suspend fun ensurePreviewInactive(): Boolean = withContext(Dispatchers.IO) {
        var pv = getPreviewStatus()
        if (!pv.active) return@withContext true

        // Si está activa, intentamos apagarla
        runCatching { togglePreview() }

        // Esperamos a que realmente quede inactiva
        repeat(10) {
            delay(250)
            pv = getPreviewStatus()
            if (!pv.active) return@withContext true
        }
        false
    }

    suspend fun startStreamingWizard(): Boolean = withContext(Dispatchers.IO) {
        val camsOk = ensureCamsRunning()
        if (!camsOk) throw RuntimeException("No pude arrancar las cams.")

        val req = Request.Builder().url(startStreamWizardUrl).post(emptyJsonBody()).build()
        client.newCall(req).execute().use { res ->
            val body = res.body?.string().orEmpty()
            parseOrThrow(res.code, body, "/start_streaming_full?type=wizard")
            true
        }
    }


    suspend fun stopStreamingWizard(): Boolean = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(stopStreamUrl).post(emptyJsonBody()).build()
        client.newCall(req).execute().use { res ->
            val body = res.body?.string().orEmpty()
            parseOrThrow(res.code, body, "/stop_streaming_full")
            true
        }
    }
    // ✅ NUEVO: arrancar SOLO cams (sin preview)
    suspend fun ensureCamsRunning(): Boolean = withContext(Dispatchers.IO) {
        var cams = getCamsStatus()
        if (cams.running) return@withContext true

        startCams()
        repeat(10) {
            delay(350)
            cams = getCamsStatus()
            if (cams.running) return@withContext true
        }
        false
    }

}
