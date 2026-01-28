package com.elitesports17.wizardlive.data.model

import com.google.gson.annotations.SerializedName
data class FollowersResponse(
    val ok: Boolean,
    val channelSlug: String,

    @SerializedName("count")
    val count: Int,

    val items: List<FollowerItem>
)

data class FollowerItem(
    val userId: String,
    val username: String
)