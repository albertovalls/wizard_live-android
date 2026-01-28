package com.elitesports17.wizardlive.data.model

data class LoginResponse(
    val token: String,
    val user: LoginUser
)

data class LoginUser(
    val id: String,
    val email: String,
    val username: String,
    val role: String
)
