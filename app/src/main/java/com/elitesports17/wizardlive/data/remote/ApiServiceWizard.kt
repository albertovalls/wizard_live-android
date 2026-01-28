package com.elitesports17.wizardlive.data.remote

import com.elitesports17.wizardlive.data.model.WizardChannel
import retrofit2.http.GET

interface ApiServiceWizard {

    @GET("channels/live")
    suspend fun getLiveChannels(): List<WizardChannel>
}
