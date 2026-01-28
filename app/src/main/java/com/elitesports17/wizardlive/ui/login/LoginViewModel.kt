package com.elitesports17.wizardlive.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitesports17.wizardlive.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {

        Log.d("LOGIN_VM", "login() llamado | email=$email")

        viewModelScope.launch {

            _uiState.value = LoginUiState.Loading
            Log.d("LOGIN_VM", "Estado -> LOADING")

            try {
                val response = repository.login(email, password)

                Log.d("LOGIN_VM", "Respuesta backend: $response")

                // ✅ NUEVO LOGIN WIZARD LIVE
                if (response.token.isNotEmpty()) {

                    Log.d("LOGIN_VM", "Login Wizard Live OK")

                    _uiState.value = LoginUiState.Success(
                        token = response.token,
                        userId = response.user.id,
                        role = response.user.role
                    )

                } else {

                    Log.e("LOGIN_VM", "Login FAIL: token vacío")

                    _uiState.value = LoginUiState.Error("Login failed")
                }

            } catch (e: Exception) {

                Log.e("LOGIN_VM", "EXCEPTION en login()", e)

                _uiState.value = LoginUiState.Error(
                    e.message ?: "Network error"
                )
            }
        }
    }
}
