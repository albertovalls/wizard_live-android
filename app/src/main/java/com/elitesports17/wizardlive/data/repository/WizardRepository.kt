package com.elitesports17.wizardlive.data.repository

import com.elitesports17.wizardlive.data.model.WizardChannel
import com.elitesports17.wizardlive.data.remote.RetrofitClient
import com.elitesports17.wizardlive.data.remote.WizardRetrofitClient


    class WizardRepository {

        suspend fun getLiveChannels(): List<WizardChannel> {
            return try {
                WizardRetrofitClient.api.getLiveChannels()
            } catch (e: Exception) {

                // 🧠 CLAVE: si el backend devuelve algo raro
                // lo tratamos como "no hay streams"
                android.util.Log.w(
                    "WIZARD_REPO",
                    "Respuesta no válida → se interpreta como lista vacía",
                    e
                )

                emptyList()
            }
        }

    }


