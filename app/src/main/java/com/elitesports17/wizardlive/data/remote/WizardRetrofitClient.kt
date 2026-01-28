package com.elitesports17.wizardlive.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WizardRetrofitClient {

    private const val BASE_URL =
        "https://livewizard.westeurope.cloudapp.azure.com/"

    private val okHttpClient = okhttp3.OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .build()

    val api: ApiServiceWizard by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // 🔥 AQUÍ
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceWizard::class.java)
    }
}
