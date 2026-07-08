package com.example.jhdkasjhd.data.repository

import com.example.jhdkasjhd.core.data.TokenStore
import com.example.jhdkasjhd.core.network.NetworkErrors
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
        val response = api.login(LoginRequest(email.trim(), password))
        persistSession(response)
        response
    }.mapError()

    suspend fun register(
        email: String,
        password: String,
        name: String,
        role: String
    ): Result<TokenResponse> = runCatching {
        val response = api.register(RegisterRequest(email.trim(), password, name, role))
        persistSession(response)
        response
    }.mapError()

    suspend fun getProfile(): Result<ProfileResponse> = runCatching {
        api.getProfile()
    }

    suspend fun updateProfile(name: String): Result<ProfileResponse> = runCatching {
        val response = api.updateProfile(ProfileUpdateRequest(name = name))
        tokenStore.updateUserName(response.name)
        response
    }

    val userBioFlow = tokenStore.userBioFlow

    suspend fun saveUserBio(bio: String) {
        tokenStore.updateUserBio(bio)
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

private fun <T> Result<T>.mapError(): Result<T> = fold(
    onSuccess = { Result.success(it) },
    onFailure = { Result.failure(IllegalStateException(NetworkErrors.userMessage(it))) }
)
