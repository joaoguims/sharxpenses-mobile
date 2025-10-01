package com.sharxpenses.data.local.dao

import androidx.room.*
import com.sharxpenses.data.local.entity.ExpenseShareEntity

@Dao
interface ExpenseShareDao {

    @Query("SELECT * FROM expense_shares WHERE expenseId = :expenseId")
    suspend fun listByExpense(expenseId: String): List<ExpenseShareEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ExpenseShareEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ExpenseShareEntity)

    @Query("DELETE FROM expense_shares WHERE expenseId IN (:expenseIds)")
    suspend fun clearByExpenses(expenseIds: List<String>)
}