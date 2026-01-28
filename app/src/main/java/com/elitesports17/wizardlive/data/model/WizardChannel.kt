package com.elitesports17.wizardlive.data.model

import com.google.gson.annotations.SerializedName

data class WizardChannel(

    @SerializedName("stream_id")
    val streamId: String,

    val channelSlug: String,
    val title: String,

    val logoUrl: String? = null,
    val ownerEmail: String? = null,
    val ownerUsername: String? = null,

    val hasPaid: Boolean = false,
    val isSubscribed: Boolean = false
)
