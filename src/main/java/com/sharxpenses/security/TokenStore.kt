package com.sharxpenses.security

import android.content.SharedPreferences
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor(
    @Named("secure_prefs") private val securePrefs: SharedPreferences
) {
    private val accessRef = AtomicReference<String?>(null)

    fun setTokens(access: String?, refresh: String?) {
        setAccessToken(access)
        setRefreshToken(refresh)
    }

    fun setAccessToken(token: String?) {
        accessRef.set(token)
    }

    fun getAccessToken(): String? = accessRef.get()

    fun setRefreshToken(token: String?) {
        securePrefs.edit().putString(KEY_REFRESH, token).apply()
    }

    fun getRefreshToken(): String? = securePrefs.getString(KEY_REFRESH, null)

    fun clear() {
        accessRef.set(null)
        securePrefs.edit().remove(KEY_REFRESH).apply()
    }

    companion object {
        private const val KEY_REFRESH = "refresh_token"
    }
}
