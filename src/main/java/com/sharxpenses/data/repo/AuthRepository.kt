package com.sharxpenses.data.repo

import android.util.Base64
import com.sharxpenses.data.local.dao.UserDao
import com.sharxpenses.data.local.entity.UserEntity
import com.sharxpenses.data.remote.AuthApi
import com.sharxpenses.data.remote.dto.ForgotPasswordRequest
import com.sharxpenses.data.remote.dto.LoginRequest
import com.sharxpenses.data.remote.dto.RefreshRequest
import com.sharxpenses.data.remote.dto.RegisterRequest
import com.sharxpenses.data.remote.dto.ResetPasswordRequest
import com.sharxpenses.security.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val tokenStore: TokenStore
) {
    suspend fun login(email: String, password: String): UserEntity = withContext(Dispatchers.IO) {
        val resp = authApi.login(LoginRequest(email, password))
        tokenStore.setTokens(resp.accessToken, resp.refreshToken)

        // Tenta extrair usuário do JWT; se não houver claims suficientes, faz fallback
        val user = decodeUserFromAccess(resp.accessToken) ?: UserEntity(
            id = email, // fallback: usa email como id
            name = email.substringBefore("@"),
            email = email,
            avatarUrl = null,
            accessToken = resp.accessToken,
            refreshToken = resp.refreshToken
        )

        // Mantém apenas 1 usuário local
        userDao.clear()
        userDao.upsert(user.copy(accessToken = resp.accessToken, refreshToken = resp.refreshToken))
        return@withContext user
    }

    suspend fun register(name: String, email: String, password: String) = withContext(Dispatchers.IO) {
        authApi.register(RegisterRequest(email = email, password = password, name = name))
        // Se o backend não fizer login automático, o app deverá chamar login() depois
    }

    suspend fun refresh(): String? = withContext(Dispatchers.IO) {
        val refresh = tokenStore.getRefreshToken() ?: return@withContext null
        val resp = authApi.refresh(RefreshRequest(refresh))
        tokenStore.setAccessToken(resp.accessToken)
        // Atualiza accessToken salvo no Room
        userDao.getCurrentUser()?.let { u ->
            userDao.upsert(u.copy(accessToken = resp.accessToken))
        }
        return@withContext resp.accessToken
    }

    suspend fun forgotPassword(email: String) = withContext(Dispatchers.IO) {
        authApi.forgotPassword(ForgotPasswordRequest(email))
    }

    suspend fun resetPassword(token: String, newPassword: String) = withContext(Dispatchers.IO) {
        authApi.resetPassword(ResetPasswordRequest(token, newPassword))
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        tokenStore.clear()
        userDao.clear()
    }

    private fun decodeUserFromAccess(access: String?): UserEntity? {
        if (access.isNullOrBlank()) return null
        return try {
            // JWT: header.payload.signature — payload é Base64URL
            val parts = access.split(".")
            if (parts.size < 2) return null
            val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val obj = JSONObject(payloadJson)

            // Assumindo que o backend define 'sub' como email (ou id), e opcionalmente name/email/avatarUrl/uid
            val sub = obj.optString("sub", null)
            val email = obj.optString("email", sub ?: "")
            val name = obj.optString("name", email.substringBefore("@"))
            val uid = obj.optString("uid", email.ifBlank { sub ?: "" })

            return UserEntity(
                id = if (uid.isBlank()) (email.ifBlank { sub ?: "" }) else uid,
                name = name,
                email = email,
                avatarUrl = obj.optString("avatarUrl", null),
                accessToken = access,
                refreshToken = tokenStore.getRefreshToken()
            )
        } catch (t: Throwable) {
            null
        }
    }
}
