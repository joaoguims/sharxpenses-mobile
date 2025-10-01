package com.sharxpenses.data.local.dao

import androidx.room.*
import com.sharxpenses.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY dateMillis DESC")
    fun observeByGroup(groupId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY dateMillis DESC")
    suspend fun listByGroup(groupId: String): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ExpenseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE groupId = :groupId")
    suspend fun clearByGroup(groupId: String)
}