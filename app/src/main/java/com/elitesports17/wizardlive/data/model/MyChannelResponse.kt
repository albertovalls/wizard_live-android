package com.elitesports17.wizardlive.data.model

data class MyChannelResponse(
    val channelSlug: String,
    val title: String,
    val isLive: Int,
    val logoUrl: String?,
    val streamKeyMasked: String
)
