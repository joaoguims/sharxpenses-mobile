package com.sharxpenses.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String? = null,
    val currency: String? = "BRL",
    val createdAt: Long? = null
)