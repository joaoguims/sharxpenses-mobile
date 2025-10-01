package com.sharxpenses.data.remote.dto

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)
