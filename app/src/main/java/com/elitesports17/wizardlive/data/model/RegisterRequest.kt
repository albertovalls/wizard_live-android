package com.elitesports17.wizardlive.data.model

data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String = "viewer",
    val username: String
)
