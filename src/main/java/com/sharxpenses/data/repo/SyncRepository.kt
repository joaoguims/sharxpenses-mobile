package com.sharxpenses.data.repo

import android.content.Context
import androidx.work.*
import com.sharxpenses.data.local.dao.PendingOperationDao
import com.sharxpenses.data.local.entity.PendingOperationEntity
import com.sharxpenses.data.remote.ExpenseApi
import com.sharxpenses.data.remote.ExpenseReq
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Repositório responsável por enfileirar e processar operações pendentes.
 * Aqui lidamos, inicialmente, com criação de despesa (EXPENSE_CREATE).
 */
@Singleton
class SyncRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val pendingDao: PendingOperationDao,
    private val moshi: Moshi,
    @Named("retrofitAuthed") retrofitAuthed: Retrofit
) {

    private val expenseApi = retrofitAuthed.create(ExpenseApi::class.java)
    private val expenseCreateAdapter = moshi.adapter(ExpenseCreatePayload::class.java)

    suspend fun queueExpenseCreate(payload: ExpenseCreatePayload) {
        val json = expenseCreateAdapter.toJson(payload)
        val op = PendingOperationEntity.new(
            type = OpTypes.EXPENSE_CREATE,
            payloadJson = json
        )
        pendingDao.insert(op)
        enqueueOneTimeSync()
    }

    /**
     * Processa todas as pendências (push).
     * Retorna a quantidade de operações processadas com sucesso.
     */
    suspend fun processPending(): Int = withContext(Dispatchers.IO) {
        var success = 0
        val list = pendingDao.listPending(limit = 100)
        for (op in list) {
            try {
                when (op.type) {
                    OpTypes.EXPENSE_CREATE -> {
                        val payload = expenseCreateAdapter.fromJson(op.payloadJson)
                            ?: error("Payload inválido")
                        val dto = expenseApi.createExpense(payload.groupId, payload.req)
                        // sucesso -> remove da fila
                        pendingDao.markDone(op.id)
                        success++
                    }
                    else -> {
                        // tipo desconhecido: descarta com marcação de erro (evita travar fila)
                        pendingDao.setStatus(op.id, "DONE", op.attempts, "Tipo desconhecido: {op.type}")
                    }
                }
            } catch (t: Throwable) {
                val attempts = op.attempts + 1
                // política simples: mantém como RETRY até X tentativas
                val status = if (attempts < 5) "RETRY" else "DONE"
                pendingDao.setStatus(op.id, status, attempts, t.message)
            }
        }
        success
    }

    /**
     * Agenda uma execução única do SyncWorker com rede disponível.
     */
    fun enqueueOneTimeSync() {
        val req = OneTimeWorkRequestBuilder<com.sharxpenses.worker.SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(appContext).enqueueUniqueWork(
            "sync-once",
            ExistingWorkPolicy.KEEP,
            req
        )
    }

    /**
     * Agenda um sync periódico (opcional), a cada 15min, com rede.
     */
    fun enqueuePeriodicSync() {
        val req = PeriodicWorkRequestBuilder<com.sharxpenses.worker.SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            "sync-periodic",
            ExistingPeriodicWorkPolicy.KEEP,
            req
        )
    }

    object OpTypes {
        const val EXPENSE_CREATE = "EXPENSE_CREATE"
    }

    data class ExpenseCreatePayload(
        val groupId: String,
        val req: ExpenseReq
    )
}