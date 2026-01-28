package com.elitesports17.wizardlive.data.repository
import com.elitesports17.wizardlive.BuildConfig
import com.elitesports17.wizardlive.data.model.*
import com.elitesports17.wizardlive.data.remote.RetrofitClient
import android.util.Log
class AuthRepository {

    suspend fun login(email: String, password: String): LoginResponse {
        Log.d("LOGIN_REPO", "Llamando API | email=$email")

        val response = RetrofitClient.api.login(
            body = LoginRequest(
                email = email,
                password = password
            )
        )

        Log.d("LOGIN_REPO", "Respuesta API: $response")
        return response
    }

}


