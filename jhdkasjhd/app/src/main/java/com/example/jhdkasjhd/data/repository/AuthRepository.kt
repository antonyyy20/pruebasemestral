package com.example.jhdkasjhd.data.repository

import com.example.jhdkasjhd.core.data.TokenStore
import com.example.jhdkasjhd.core.network.QuickvntApi
import com.example.jhdkasjhd.data.dto.LoginRequest
import com.example.jhdkasjhd.data.dto.ProfileResponse
import com.example.jhdkasjhd.data.dto.ProfileUpdateRequest
import com.example.jhdkasjhd.data.dto.RegisterRequest
import com.example.jhdkasjhd.data.dto.TokenResponse

class AuthRepository(
    private val api: QuickvntApi,
    private val tokenStore: TokenStore
) {
    val sessionFlow = tokenStore.sessionFlow

    suspend fun login(email: String, password: String): Result<TokenResponse> = runCatching {
        val response = api.login(LoginRequest(email, password))
        persistSession(response)
        response
    }

    suspend fun register(
        email: String,
        password: String,
        name: String,
        role: String
    ): Result<TokenResponse> = runCatching {
        val response = api.register(RegisterRequest(email, password, name, role))
        persistSession(response)
        response
    }

    suspend fun getProfile(): Result<ProfileResponse> = runCatching {
        api.getProfile()
    }

    suspend fun updateProfile(name: String): Result<ProfileResponse> = runCatching {
        api.updateProfile(ProfileUpdateRequest(name = name))
    }

    suspend fun logout() {
        tokenStore.clearSession()
    }

    private suspend fun persistSession(response: TokenResponse) {
        tokenStore.saveSession(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            userId = response.userId,
            name = response.name,
            role = response.role
        )
    }
}
