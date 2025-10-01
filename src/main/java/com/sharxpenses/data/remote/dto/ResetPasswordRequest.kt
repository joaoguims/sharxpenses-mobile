package com.sharxpenses.data.remote.dto

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)
