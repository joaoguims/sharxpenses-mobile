package com.sharxpenses.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharxpenses.data.repo.AuthRepository
import com.sharxpenses.ui.login.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value)
    }

    fun login() {
        val email = _state.value.email.trim()
        val pass = _state.value.password
        if (email.isBlank() || pass.isBlank()) {
            _state.value = _state.value.copy(error = "Informe e-mail e senha.")
            return
        }
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(loading = true, error = null)
                authRepository.login(email, pass)
                _events.emit(UiEvent.NavigateHome)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(error = t.message ?: "Falha ao autenticar.")
            } finally {
                _state.value = _state.value.copy(loading = false)
            }
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }

    sealed interface UiEvent {
        data object NavigateHome : UiEvent
    }
}