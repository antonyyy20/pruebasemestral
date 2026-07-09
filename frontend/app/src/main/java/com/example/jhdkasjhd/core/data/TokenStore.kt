package com.example.jhdkasjhd.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quickvnt_session")

class TokenStore(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var cachedRefreshToken: String? = null

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_ROLE = stringPreferencesKey("user_role")
        val USER_BIO = stringPreferencesKey("user_bio")
    }

    val sessionFlow: Flow<UserSession?> = appContext.dataStore.data.map { prefs ->
        val accessToken = prefs[Keys.ACCESS_TOKEN]
        val refreshToken = prefs[Keys.REFRESH_TOKEN]
        val userId = prefs[Keys.USER_ID]
        val userName = prefs[Keys.USER_NAME]
        val userRole = prefs[Keys.USER_ROLE]

        cachedAccessToken = accessToken
        cachedRefreshToken = refreshToken

        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank() || userId.isNullOrBlank()) {
            null
        } else {
            UserSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = userId,
                name = userName.orEmpty(),
                role = userRole.orEmpty()
            )
        }
    }

    init {
        runBlocking(Dispatchers.IO) {
            preloadCache()
        }
        scope.launch {
            sessionFlow.first()
        }
    }

    private suspend fun preloadCache() {
        val prefs = appContext.dataStore.data.first()
        cachedAccessToken = prefs[Keys.ACCESS_TOKEN]
        cachedRefreshToken = prefs[Keys.REFRESH_TOKEN]
    }

    fun getAccessTokenSync(): String? = cachedAccessToken

    fun getRefreshTokenSync(): String? = cachedRefreshToken

    suspend fun getAccessToken(): String? =
        appContext.dataStore.data.first()[Keys.ACCESS_TOKEN]?.also { cachedAccessToken = it }

    suspend fun getRefreshToken(): String? =
        appContext.dataStore.data.first()[Keys.REFRESH_TOKEN]?.also { cachedRefreshToken = it }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: String,
        name: String,
        role: String
    ) {
        cachedAccessToken = accessToken
        cachedRefreshToken = refreshToken
        appContext.dataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = accessToken
            prefs[Keys.REFRESH_TOKEN] = refreshToken
            prefs[Keys.USER_ID] = userId
            prefs[Keys.USER_NAME] = name
            prefs[Keys.USER_ROLE] = role
        }
    }

    suspend fun clearSession() {
        cachedAccessToken = null
        cachedRefreshToken = null
        appContext.dataStore.edit { it.clear() }
    }

    suspend fun updateUserName(name: String) {
        appContext.dataStore.edit { prefs ->
            prefs[Keys.USER_NAME] = name
        }
    }

    val userBioFlow: Flow<String> = appContext.dataStore.data.map { prefs ->
        prefs[Keys.USER_BIO].orEmpty()
    }

    suspend fun updateUserBio(bio: String) {
        appContext.dataStore.edit { prefs ->
            prefs[Keys.USER_BIO] = bio
        }
    }
}

data class UserSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val name: String,
    val role: String
) {
    val isOrganizer: Boolean get() = role == "ORGANIZER"
    val isAttendee: Boolean get() = role == "ATTENDEE"
}
