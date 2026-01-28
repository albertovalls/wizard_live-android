package com.elitesports17.wizardlive.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitesports17.wizardlive.data.model.WizardChannel
import com.elitesports17.wizardlive.data.repository.WizardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class WizardUiState {
    object Loading : WizardUiState()
    data class Success(val channels: List<WizardChannel>) : WizardUiState()
    object Empty : WizardUiState()
    data class Error(val message: String) : WizardUiState()
}

class HomeViewModel : ViewModel() {

    private val repository = WizardRepository()

    private val _wizardState = MutableStateFlow<WizardUiState>(WizardUiState.Loading)
    val wizardState: StateFlow<WizardUiState> = _wizardState



    fun loadWizardChannels() {
        viewModelScope.launch {
            try {
                android.util.Log.d("WIZARD_VM", "📡 Llamando a /channels/live")
                val channels = repository.getLiveChannels()

                android.util.Log.d(
                    "WIZARD_VM",
                    "✅ Respuesta OK: ${channels.size} streams"
                )

                _wizardState.value =
                    if (channels.isEmpty()) WizardUiState.Empty
                    else WizardUiState.Success(channels)

            } catch (e: Exception) {

                // 🔥 ESTO ES CLAVE
                android.util.Log.e(
                    "WIZARD_VM",
                    "❌ Error REAL en getLiveChannels()",
                    e
                )

                _wizardState.value =
                    WizardUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

}
