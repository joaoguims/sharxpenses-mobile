package com.sharxpenses.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sharxpenses.data.repo.SyncRepository
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker que processa a fila de operações pendentes.
 * Não depende de injeção direta no construtor (evita precisar de androidx.hilt:hilt-work),
 * usando EntryPointAccessors para obter o repositório.
 */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                SyncEntryPoint::class.java
            )
            val repo = entryPoint.syncRepository()
            val processed = repo.processPending()
            if (processed > 0) Result.success() else Result.success()
        } catch (t: Throwable) {
            Result.retry()
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncEntryPoint {
        fun syncRepository(): SyncRepository
    }
}