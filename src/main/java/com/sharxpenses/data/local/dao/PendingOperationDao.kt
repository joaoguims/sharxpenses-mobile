package com.sharxpenses.data.local.dao

import androidx.room.*
import com.sharxpenses.data.local.entity.PendingOperationEntity

@Dao
interface PendingOperationDao {

    @Query("SELECT * FROM pending_ops WHERE status IN ('PENDING','RETRY') ORDER BY createdAtMillis ASC, attempts ASC LIMIT :limit")
    suspend fun listPending(limit: Int = 50): List<PendingOperationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(op: PendingOperationEntity)

    @Update
    suspend fun update(op: PendingOperationEntity)

    @Query("DELETE FROM pending_ops WHERE id = :id")
    suspend fun deleteById(id: String)

    @Transaction
    suspend fun markDone(id: String) = deleteById(id)

    @Query("UPDATE pending_ops SET status = :status, attempts = :attempts, lastError = :error WHERE id = :id")
    suspend fun setStatus(id: String, status: String, attempts: Int, error: String?)
}