package com.elitesports17.wizardlive.data.model

data class ProfileMeResponse(
    val username: String,
    val email: String,
    val role: String,
    val createdAt: String,
    val logo_url: String?
)
