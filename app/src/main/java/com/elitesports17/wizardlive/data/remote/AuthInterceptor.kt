package com.elitesports17.wizardlive.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import com.elitesports17.wizardlive.ui.util.UserSession

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val token = UserSession.getCachedToken()

        android.util.Log.d(
            "AUTH",
            "Authorization enviado = Bearer $token"
        )

        return chain.proceed(
            chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        )
    }
}
