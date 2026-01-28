package com.elitesports17.wizardlive.data.model

data class RecoverPasswordRequest(
    val email: String
)

data class RecoverPasswordResponse(
    val message: String? = null,
    val status: String? = null,
    val error: String? = null
)