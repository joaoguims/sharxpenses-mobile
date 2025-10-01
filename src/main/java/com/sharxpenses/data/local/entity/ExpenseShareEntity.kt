package com.sharxpenses.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_shares")
data class ExpenseShareEntity(
    @PrimaryKey val id: String,
    val expenseId: String,
    val userId: String,
    val amountDueCents: Long,
    val amountPaidCents: Long = 0,
    val status: String = "OPEN"
)