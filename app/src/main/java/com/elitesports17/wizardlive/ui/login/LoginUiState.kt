package com.elitesports17.wizardlive.ui.login



sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(
        val token: String,
        val userId: String,
        val role: String
    ) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

