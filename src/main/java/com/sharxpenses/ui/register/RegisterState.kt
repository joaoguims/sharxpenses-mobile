package com.sharxpenses.ui.register

data class RegisterState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirm: String = "",
    val loading: Boolean = false,
    val error: String? = null
)