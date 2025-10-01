package com.sharxpenses.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,            // mesmo id do backend
    val name: String,
    val email: String,
    val avatarUrl: String? = null,

    // Campos úteis para auth local (opcionais, mas comuns em offline-first)
    val accessToken: String? = null,
    val refreshToken: String? = null
)
