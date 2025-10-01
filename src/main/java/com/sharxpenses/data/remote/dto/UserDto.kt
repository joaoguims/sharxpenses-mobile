package com.sharxpenses.data.remote.dto

data class UserDto(
    val id: String,
    val email: String,
    val name: String? = null,
    val avatarUrl: String? = null,
    val currency: String? = null,
    val notificationsEnabled: Boolean? = null
)