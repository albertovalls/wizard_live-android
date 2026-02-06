package com.elitesports17.wizardlive.data.remote

import com.elitesports17.wizardlive.data.model.*
import retrofit2.http.*
import retrofit2.Response

interface ApiService {

    // 🔐 LOGIN (YA EXISTE → NO TOCAR)
    @POST("login")
    suspend fun login(
        @Body body: LoginRequest
    ): LoginResponse
    // 👥 FOLLOWERS DEL CANAL DEL USUARIO
    @GET("channels/me/followers")
    suspend fun getMyFollowers(
        @Header("Authorization") token: String
    ): FollowersResponse

    @GET("me/subscriptions")
    suspend fun getMySubscriptions(
        @Header("Authorization") token: String
    ): SubscriptionsResponse

    @GET("channels/me")
    suspend fun getMyChannel(
        @Header("Authorization") token: String
    ): MyChannelResponse
    @POST("register")
    suspend fun register(
        @Body body: RegisterRequest
    ): RegisterResponse
    @POST("auth/recover-password")
    suspend fun recoverPassword(
        @Body body: RecoverPasswordRequest
    ): RecoverPasswordResponse
    @GET("me/profile")
    suspend fun getMyProfile(
        @Header("Authorization") auth: String
    ): ProfileMeResponse




}
