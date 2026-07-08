package com.example.jhdkasjhd.core

import android.content.Context
import com.example.jhdkasjhd.BuildConfig
import com.example.jhdkasjhd.core.data.TokenStore
import com.example.jhdkasjhd.core.network.AuthInterceptor
import com.example.jhdkasjhd.core.network.NetworkDns
import com.example.jhdkasjhd.core.network.QuickvntApi
import com.example.jhdkasjhd.data.repository.AuthRepository
import com.example.jhdkasjhd.data.repository.EventRepository
import com.example.jhdkasjhd.data.repository.TicketRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val tokenStore = TokenStore(appContext)

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private lateinit var api: QuickvntApi

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .dns(NetworkDns.fallback)
        .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))
        .addInterceptor(RetryOnNetworkFailureInterceptor())
        .addInterceptor(AuthInterceptor(tokenStore) { api })
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .build()

    init {
        api = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(QuickvntApi::class.java)
    }

    val authRepository = AuthRepository(api, tokenStore)
    val eventRepository = EventRepository(api)
    val ticketRepository = TicketRepository(api)
}

private class RetryOnNetworkFailureInterceptor(
    private val maxRetries: Int = 1
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var lastError: IOException? = null
        repeat(maxRetries + 1) { attempt ->
            try {
                return chain.proceed(chain.request())
            } catch (error: IOException) {
                lastError = error
                val retryable = error is UnknownHostException || error is SocketTimeoutException
                if (!retryable || attempt == maxRetries) throw error
            }
        }
        throw lastError ?: IOException("Network request failed")
    }
}
