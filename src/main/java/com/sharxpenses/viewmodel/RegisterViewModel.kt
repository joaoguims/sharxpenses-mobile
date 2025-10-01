package com.sharxpenses.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharxpenses.data.repo.AuthRepository
import com.sharxpenses.ui.register.RegisterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    fun onNameChange(v: String) { _state.value = _state.value.copy(name = v) }
    fun onEmailChange(v: String) { _state.value = _state.value.copy(email = v) }
    fun onPasswordChange(v: String) { _state.value = _state.value.copy(password = v) }
    fun onConfirmChange(v: String) { _state.value = _state.value.copy(confirm = v) }

    fun register() {
        val s = _state.value
        if (s.name.isBlank() || s.email.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = "Preencha nome, e-mail e senha.")
            return
        }
        if (s.password != s.confirm) {
            _state.value = s.copy(error = "Confirmação de senha não confere.")
            return
        }
        viewModelScope.launch {
            try {
                _state.value = s.copy(loading = true, error = null)
                authRepository.register(s.name.trim(), s.email.trim(), s.password)
                // após registrar, já faz login para manter UX simples
                authRepository.login(s.email.trim(), s.password)
                _events.emit(UiEvent.NavigateHome)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(error = t.message ?: "Falha no cadastro.")
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