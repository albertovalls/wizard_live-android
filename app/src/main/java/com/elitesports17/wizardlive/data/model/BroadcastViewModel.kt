package com.elitesports17.wizardlive.data.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitesports17.wizardlive.data.remote.RetrofitClient
import com.elitesports17.wizardlive.ui.util.UserSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BroadcastUiState(
    val connectedToWizardCam: Boolean = false,
    val loading: Boolean = false,
    val status: JetsonStatus? = null,
    val wizardUsername: String = "",
    val disableStream: Boolean = true,
    val streamingBusy: Boolean = false,
    val error: String? = null,

    // title post
    val titleBusy: Boolean = false,
    val titleError: String? = null,

    // preview
    val previewBusy: Boolean = false,
    val previewActive: Boolean = false,
    val previewError: String? = null
)

class BroadcastViewModel(
    private val api: JetsonApi = JetsonApi()
) : ViewModel() {

    private val _ui = MutableStateFlow(BroadcastUiState(loading = true))
    val ui: StateFlow<BroadcastUiState> = _ui

    init {
        viewModelScope.launch {
            while (true) {
                refresh()
                delay(3000)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                val status = api.getStatus()
                val wizard = api.getWizardInfo()
                val pv = runCatching { api.getPreviewStatus() }.getOrNull()

                Log.d(
                    "BROADCAST",
                    "REFRESH status='${status.status}' trimmed='${status.status.trim()}' wizardOk=${wizard.ok} user='${wizard.username}' preview=${pv?.active}"
                )

                _ui.value = _ui.value.copy(
                    connectedToWizardCam = true,
                    loading = false,
                    status = status,
                    wizardUsername = wizard.username,
                    disableStream = !wizard.ok,
                    previewActive = pv?.active ?: _ui.value.previewActive,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("BROADCAST", "REFRESH FAILED", e)

                _ui.value = BroadcastUiState(
                    connectedToWizardCam = false,
                    loading = false,
                    error = e.message
                )
            }
        }
    }


    fun startStreaming() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(streamingBusy = true, error = null)
            try {
                Log.d("BROADCAST", "START_STREAMING -> calling JetsonApi.startStreamingWizard()")

                api.startStreamingWizard()
                Log.d("BROADCAST", "START_STREAMING -> startStreamingWizard() returned OK")

                refresh()
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    error = "startStreamingWizard falló: ${e.message}"
                )
            } finally {
                _ui.value = _ui.value.copy(streamingBusy = false)
            }
        }
    }


    fun stopStreaming() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(streamingBusy = true, error = null)
            try {
                api.stopStreamingWizard()
                refresh()
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(error = e.message)
            } finally {
                _ui.value = _ui.value.copy(streamingBusy = false)
            }
        }
    }

    fun ensurePreview() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(previewBusy = true, previewError = null)
            try {
                val ok = api.ensurePreviewActive()
                _ui.value = _ui.value.copy(previewActive = ok)
                if (!ok) _ui.value = _ui.value.copy(previewError = "Preview no activó.")
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(previewError = e.message ?: "Error activando preview.")
            } finally {
                _ui.value = _ui.value.copy(previewBusy = false)
            }
        }
    }

    fun stopPreview() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(previewBusy = true, previewError = null)
            try {
                val ok = api.ensurePreviewInactive()
                if (!ok) _ui.value = _ui.value.copy(previewError = "No pude parar la preview.")
                _ui.value = _ui.value.copy(previewActive = false)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(previewError = e.message ?: "Error parando preview.")
            } finally {
                _ui.value = _ui.value.copy(previewBusy = false)
            }
        }
    }

    /**
     * ✅ NUEVO: envía el título al backend (con token guardado) y luego inicia el streaming local.
     * - Si el title está vacío, NO llamamos al endpoint y arrancamos igualmente (puedes cambiarlo si quieres obligatorio).
     * - Token: primero cached, si no existe lo lee de DataStore.
     */
    fun postTitleAndStart(context: Context, title: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(
                titleBusy = true,
                titleError = null,
                error = null
            )

            try {
                val rawToken = UserSession.getCachedToken()
                    ?: UserSession.getToken(context)

                val token = rawToken
                    ?.removePrefix("Bearer ")
                    ?.trim()

                if (token.isNullOrBlank()) {
                    throw RuntimeException("No hay token. Inicia sesión de nuevo.")
                }

                val cleanTitle = title.trim()
                Log.d("BROADCAST", "POST_TITLE_AND_START title='$cleanTitle' tokenLen=${token.length}")
                // 1) Intentar actualizar título (si hay)
                if (cleanTitle.isNotEmpty()) {
                    Log.d("BROADCAST", "UPDATE_TITLE -> calling /update_stream_title")
                    val ok = api.updateStreamTitle(token = token, title = cleanTitle)
                    Log.d("BROADCAST", "UPDATE_TITLE -> result ok=$ok")

                    if (!ok) {
                        throw RuntimeException("No pude guardar el título en la WizCam.")
                    }

                }
                Log.d("BROADCAST", "postTitleAndStart title='$cleanTitle' tokenLen=${token.length}")

                // 2) Arrancar streaming SIEMPRE (así aislamos el problema real)
                startStreaming()

            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    titleError = e.message ?: "Error enviando título",
                    error = e.message
                )
            } finally {
                _ui.value = _ui.value.copy(titleBusy = false)
            }
        }
    }


    fun streamUrls(): List<String> = api.streamCandidates()
}
