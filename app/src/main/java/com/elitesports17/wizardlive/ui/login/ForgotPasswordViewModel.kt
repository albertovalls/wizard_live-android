package com.elitesports17.wizardlive.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitesports17.wizardlive.data.model.RecoverPasswordRequest
import com.elitesports17.wizardlive.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    data class Success(val message: String) : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

class ForgotPasswordViewModel(
    private val api: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val state: StateFlow<ForgotPasswordState> = _state

    fun recover(email: String) {
        viewModelScope.launch {
            _state.value = ForgotPasswordState.Loading
            try {
                val response = api.recoverPassword(
                    RecoverPasswordRequest(email)
                )

                if (response.status == "error") {
                    _state.value = ForgotPasswordState.Error("User not found")
                } else {
                    _state.value = ForgotPasswordState.Success(
                        response.message ?: "Password recovery email sent."
                    )
                }
            } catch (e: Exception) {
                _state.value = ForgotPasswordState.Error("Something went wrong")
            }
        }
    }
}
