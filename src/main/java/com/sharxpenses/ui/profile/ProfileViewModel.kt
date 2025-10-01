package com.sharxpenses.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharxpenses.data.repo.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val email: String = "",
    val name: String = "",
    val avatarUrl: String = "",
    val currency: String = "BRL",
    val notifyEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository
) : ViewModel() {

    var uiState = androidx.compose.runtime.mutableStateOf(ProfileUiState())
        private set

    init { load() }

    fun load() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true, error = null, message = null)
            try {
                val local = userRepo.getLocalUser()
                if (local != null) {
                    uiState.value = uiState.value.copy(
                        email = local.email,
                        name = local.name,
                        avatarUrl = local.avatarUrl,
                        currency = userRepo.getCurrencyPref("BRL"),
                        notifyEnabled = userRepo.getNotifyPref(true)
                    )
                }
                // sincroniza do backend
                val me = userRepo.getMeRemoteSaveLocal()
                uiState.value = uiState.value.copy(
                    email = me.email,
                    name = me.name,
                    avatarUrl = me.avatarUrl,
                    currency = userRepo.getCurrencyPref("BRL"),
                    notifyEnabled = userRepo.getNotifyPref(true),
                    isLoading = false
                )
            } catch (t: Throwable) {
                uiState.value = uiState.value.copy(isLoading = false, error = t.message ?: "Erro ao carregar perfil")
            }
        }
    }

    fun onNameChange(v: String) { uiState.value = uiState.value.copy(name = v) }
    fun onAvatarChange(v: String) { uiState.value = uiState.value.copy(avatarUrl = v) }
    fun onCurrencyChange(v: String) { uiState.value = uiState.value.copy(currency = v) }
    fun onNotifyToggle(v: Boolean) { uiState.value = uiState.value.copy(notifyEnabled = v) }

    fun save() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true, error = null, message = null)
            try {
                val s = uiState.value
                userRepo.updateProfile(name = s.name, avatarUrl = s.avatarUrl)
                userRepo.updatePreferences(currency = s.currency, notifyEnabled = s.notifyEnabled)
                uiState.value = uiState.value.copy(isLoading = false, message = "Perfil atualizado")
            } catch (t: Throwable) {
                uiState.value = uiState.value.copy(isLoading = false, error = t.message ?: "Erro ao salvar")
            }
        }
    }
}