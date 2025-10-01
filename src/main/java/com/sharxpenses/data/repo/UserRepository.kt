package com.sharxpenses.data.repo

import android.content.Context
import android.content.SharedPreferences
import com.sharxpenses.data.local.dao.UserDao
import com.sharxpenses.data.local.entity.UserEntity
import com.sharxpenses.data.remote.UserApi
import com.sharxpenses.data.remote.dto.UpdateUserRequest
import com.sharxpenses.data.remote.dto.UserDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    @ApplicationContext private val appContext: Context,
    @Named("retrofitAuthed") retrofitAuthed: Retrofit,
    @Named("io") private val io: CoroutineDispatcher
) {
    private val api = retrofitAuthed.create(UserApi::class.java)
    private val prefs: SharedPreferences by lazy {
        appContext.getSharedPreferences("sharxpenses_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREF_CURRENCY = "pref_currency"
        private const val PREF_NOTIFY = "pref_notify"
    }

    suspend fun getMeRemoteSaveLocal(): UserEntity = withContext(io) {
        val dto = api.me()
        val entity = dto.toEntity()
        userDao.upsert(entity)
        // se backend retornar preferências, salva localmente
        dto.currency?.let { setCurrencyPref(it) }
        dto.notificationsEnabled?.let { setNotifyPref(it) }
        entity
    }

    suspend fun updateProfile(name: String?, avatarUrl: String?): UserEntity = withContext(io) {
        val dto = api.update(UpdateUserRequest(name = name, avatarUrl = avatarUrl))
        val entity = dto.toEntity()
        userDao.upsert(entity)
        entity
    }

    suspend fun updatePreferences(currency: String, notifyEnabled: Boolean): UserEntity = withContext(io) {
        // envia também para o backend (se suportado) para manter consistência
        val dto = api.update(UpdateUserRequest(currency = currency, notificationsEnabled = notifyEnabled))
        // persiste localmente
        setCurrencyPref(currency)
        setNotifyPref(notifyEnabled)
        val entity = dto.toEntity()
        userDao.upsert(entity)
        entity
    }

    suspend fun getLocalUser(): UserEntity? = withContext(io) { userDao.getSingle() }

    fun getCurrencyPref(default: String = "BRL"): String = prefs.getString(PREF_CURRENCY, default) ?: default
    fun getNotifyPref(default: Boolean = true): Boolean = prefs.getBoolean(PREF_NOTIFY, default)

    private fun setCurrencyPref(value: String) {
        prefs.edit().putString(PREF_CURRENCY, value).apply()
    }
    private fun setNotifyPref(value: Boolean) {
        prefs.edit().putBoolean(PREF_NOTIFY, value).apply()
    }

    private fun UserDto.toEntity(): UserEntity =
        UserEntity(
            id = this.id,
            email = this.email,
            name = this.name ?: "",
            avatarUrl = this.avatarUrl ?: ""
        )
}