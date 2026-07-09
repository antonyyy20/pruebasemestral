package com.example.jhdkasjhd.core.network

import com.example.jhdkasjhd.core.data.TokenStore
import com.example.jhdkasjhd.data.dto.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenStore: TokenStore,
    private val apiProvider: () -> QuickvntApi
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val accessToken = tokenStore.getAccessTokenSync()
            ?: runBlocking { tokenStore.getAccessToken() }

        val authenticatedRequest = if (!accessToken.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            original
        }

        val response = chain.proceed(authenticatedRequest)

        if (response.code != 401 || accessToken.isNullOrBlank()) {
            return response
        }

        response.close()

        val refreshToken = tokenStore.getRefreshTokenSync()
            ?: runBlocking { tokenStore.getRefreshToken() }
            ?: return chain.proceed(original)

        return runBlocking {
            try {
                val refreshResponse = apiProvider().refresh(RefreshRequest(refreshToken))

                tokenStore.saveSession(
                    accessToken = refreshResponse.accessToken,
                    refreshToken = refreshResponse.refreshToken,
                    userId = refreshResponse.userId,
                    name = refreshResponse.name,
                    role = refreshResponse.role
                )

                val retryRequest = original.newBuilder()
                    .header("Authorization", "Bearer ${refreshResponse.accessToken}")
                    .build()

                chain.proceed(retryRequest)
            } catch (_: Exception) {
                tokenStore.clearSession()
                chain.proceed(original)
            }
        }
    }
}
