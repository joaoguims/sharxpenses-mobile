package com.sharxpenses.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabela de operações pendentes para sync offline-first.
 * type: tipo da operação (ex: EXPENSE_CREATE)
 * payloadJson: JSON com o payload específico da operação
 * status: PENDING | RETRY | DONE
 */
@Entity(
    tableName = "pending_ops",
    indices = [
        Index(value = ["status"]),
        Index(value = ["type"])
    ]
)
data class PendingOperationEntity(
    @PrimaryKey val id: String,
    val type: String,
    val payloadJson: String,
    val createdAtMillis: Long,
    val status: String,
    val attempts: Int,
    val lastError: String? = null
) {
    companion object {
        fun new(type: String, payloadJson: String): PendingOperationEntity =
            PendingOperationEntity(
                id = java.util.UUID.randomUUID().toString(),
                type = type,
                payloadJson = payloadJson,
                createdAtMillis = System.currentTimeMillis(),
                status = "PENDING",
                attempts = 0,
                lastError = null
            )
    }
}