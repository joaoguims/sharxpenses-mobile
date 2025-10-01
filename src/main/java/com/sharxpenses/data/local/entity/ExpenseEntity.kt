package com.sharxpenses.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val payerId: String,
    val amountCents: Long,
    val category: String,
    val dateMillis: Long,
    val createdAtMillis: Long,
    val description: String? = null
)