package com.example.jhdkasjhd.core

import android.content.Context
import com.example.jhdkasjhd.BuildConfig
import com.example.jhdkasjhd.core.data.TokenStore
import com.example.jhdkasjhd.core.network.AuthInterceptor
import com.example.jhdkasjhd.core.network.QuickvntApi
import com.example.jhdkasjhd.data.repository.AuthRepository
import com.example.jhdkasjhd.data.repository.EventRepository
import com.example.jhdkasjhd.data.repository.TicketRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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
        .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))
        .addInterceptor(AuthInterceptor(tokenStore) { api })
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
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
