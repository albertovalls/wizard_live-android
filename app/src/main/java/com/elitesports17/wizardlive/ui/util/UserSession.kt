package com.elitesports17.wizardlive.ui.util

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore("user_session")
private val REMEMBER_ME = booleanPreferencesKey("remember_me")
object UserSession {

    private val LOGGED_IN = booleanPreferencesKey("logged_in")
    private val TOKEN = stringPreferencesKey("token")
    private val USER_ID = stringPreferencesKey("user_id")
    private val ROLE = stringPreferencesKey("role")
    // 🔥 TOKEN EN MEMORIA (NECESARIO PARA INTERCEPTOR)
    private var cachedToken: String? = null

    // =====================================================
    // ✅ GUARDAR SESIÓN
    // =====================================================
    suspend fun saveSession(
        context: Context,
        token: String,
        userId: String,
        role: String,
        rememberMe: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[LOGGED_IN] = true
            prefs[TOKEN] = token
            prefs[USER_ID] = userId
            prefs[ROLE] = role
            prefs[REMEMBER_ME] = rememberMe
        }

        cachedToken = token
    }

    // =====================================================
    // ❌ CERRAR SESIÓN
    // =====================================================
    suspend fun clearSession(context: Context) {
        context.dataStore.edit { it.clear() }
        cachedToken = null
    }

    suspend fun shouldAutoLogin(context: Context): Boolean {
        val prefs = context.dataStore.data.first()
        return (prefs[LOGGED_IN] == true) &&
                (prefs[REMEMBER_ME] == true) &&
                !prefs[TOKEN].isNullOrEmpty()
    }

    // =====================================================
    // 🔐 TOKEN
    // =====================================================
    suspend fun getToken(context: Context): String? {
        val prefs = context.dataStore.data.first()
        return prefs[TOKEN]
    }

    // 👉 USADO POR AuthInterceptor (NO suspend)
    fun getCachedToken(): String? = cachedToken

    fun setCachedToken(token: String?) {
        cachedToken = token
    }

    // =====================================================
    // 👤 USER ID
    // =====================================================
    suspend fun getUserId(context: Context): String? {
        val prefs = context.dataStore.data.first()
        return prefs[USER_ID]
    }

    suspend fun getRole(context: Context): String {
        val prefs = context.dataStore.data.first()
        return prefs[ROLE] ?: "viewer"
    }

    // =====================================================
    // ✅ ESTADO DE SESIÓN
    // =====================================================
    suspend fun isLoggedIn(context: Context): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[LOGGED_IN] ?: false
    }
}
