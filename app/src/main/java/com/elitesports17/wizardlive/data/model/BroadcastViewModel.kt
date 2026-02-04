package com.elitesports17.wizardlive.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                api.startStreamingWizard()
                refresh()
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(error = e.message)
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

    // ✅ NUEVO: parar preview de verdad
    fun stopPreview() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(previewBusy = true, previewError = null)
            try {
                val ok = api.ensurePreviewInactive()
                _ui.value = _ui.value.copy(previewActive = !ok) // si ok==true => inactiva
                if (!ok) _ui.value = _ui.value.copy(previewError = "No pude parar la preview.")
                else _ui.value = _ui.value.copy(previewActive = false)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(previewError = e.message ?: "Error parando preview.")
            } finally {
                _ui.value = _ui.value.copy(previewBusy = false)
            }
        }
    }

    fun streamUrls(): List<String> = api.streamCandidates()
}
