package com.sharxpenses.data.remote.dto

/**
 * Body flexível: só enviaremos o que o usuário alterou.
 */
data class UpdateUserRequest(
    val name: String? = null,
    val avatarUrl: String? = null,
    val currency: String? = null,
    val notificationsEnabled: Boolean? = null
)