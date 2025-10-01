package com.sharxpenses.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sharxpenses.data.local.dao.ExpenseDao
import com.sharxpenses.data.local.dao.ExpenseShareDao
import com.sharxpenses.data.local.dao.PendingOperationDao
import com.sharxpenses.data.local.dao.UserDao
import com.sharxpenses.data.local.entity.ExpenseEntity
import com.sharxpenses.data.local.entity.ExpenseShareEntity
import com.sharxpenses.data.local.entity.PendingOperationEntity
import com.sharxpenses.data.local.entity.UserEntity

import com.sharxpenses.data.local.entity.GroupEntity
import com.sharxpenses.data.local.dao.GroupDao
@Database(
    entities = [
        UserEntity,
        ExpenseEntity,
        ExpenseShareEntity,
        PendingOperationEntity
    ],
    version = 3, // incrementamos a versÃ£o devido Ã  nova tabela
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun expenseShareDao(): ExpenseShareDao
    abstract fun pendingOperationDao(): PendingOperationDao
    abstract fun groupDao(): GroupDao
}