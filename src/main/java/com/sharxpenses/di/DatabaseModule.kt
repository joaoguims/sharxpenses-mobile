package com.sharxpenses.di

import android.content.Context
import androidx.room.Room
import com.sharxpenses.data.local.AppDatabase
import com.sharxpenses.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DB_NAME
        )
        // Para MVP/Dev: se houver mudança de schema, recria o DB (evita crash).
        // Em produção, substitua por Migrations reais.
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
}
