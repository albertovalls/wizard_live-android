package com.elitesports17.wizardlive.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitesports17.wizardlive.data.model.RegisterRequest
import com.elitesports17.wizardlive.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

class RegisterViewModel(
    private val api: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val state: StateFlow<RegisterUiState> = _state

    fun register(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (password.length < 8) {
            _state.value = RegisterUiState.Error("Password must be at least 8 characters")
            return
        }

        if (password != confirmPassword) {
            _state.value = RegisterUiState.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            try {
                _state.value = RegisterUiState.Loading

                api.register(
                    RegisterRequest(
                        email = email,
                        password = password,
                        username = username
                    )
                )

                _state.value = RegisterUiState.Success
            } catch (e: Exception) {
                _state.value = RegisterUiState.Error("Registration failed")
            }
        }
    }
}
