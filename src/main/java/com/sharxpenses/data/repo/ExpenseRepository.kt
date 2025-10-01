package com.sharxpenses.data.repo

import com.sharxpenses.data.local.dao.ExpenseDao
import com.sharxpenses.data.local.dao.ExpenseShareDao
import com.sharxpenses.data.local.entity.ExpenseEntity
import com.sharxpenses.data.local.entity.ExpenseShareEntity
import com.sharxpenses.data.remote.*
import com.sharxpenses.domain.Splitter
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.math.round

@Singleton
class ExpenseRepository @Inject constructor(
    @Named("retrofitAuthed") retrofitAuthed: Retrofit,
    private val expenseDao: ExpenseDao,
    private val shareDao: ExpenseShareDao,
    private val splitter: Splitter
) {

    private val api = retrofitAuthed.create(ExpenseApi::class.java)

    fun observeGroupExpenses(groupId: String): Flow<List<ExpenseEntity>> =
        expenseDao.observeByGroup(groupId)

    suspend fun refreshGroupExpenses(groupId: String) {
        val dtos = api.getExpenses(groupId)
        val expenses = dtos.map { it.toEntity() }
        val shares = dtos.flatMap { dto -> dto.shares.map { it.toEntity() } }
        // opcional: limpar e repor
        val ids = expenses.map { it.id }
        shareDao.clearByExpenses(ids)
        expenseDao.clearByGroup(groupId)
        expenseDao.upsertAll(expenses)
        shareDao.upsertAll(shares)
    }

    suspend fun createExpense(
        groupId: String,
        payerId: String,
        amount: Double,
        category: String,
        dateMillis: Long,
        participants: List<String>,
        mode: SplitMode,
        percents: List<PercentShare>? = null,
        custom: List<CustomShare>? = null,
        description: String? = null
    ): ExpenseEntity {
        // Calcula rateio localmente (opcional para exibir preview / consistência)
        // No corpo enviado, também vai a estrutura para o backend.
        val req = ExpenseReq(
            amount = amount,
            category = category,
            date = dateMillis,
            payerId = payerId,
            participants = participants,
            splitMode = mode,
            customShares = custom,
            percents = percents,
            description = description
        )
        val dto = api.createExpense(groupId, req)
        val entity = dto.toEntity()
        val shares = dto.shares.map { it.toEntity() }

        expenseDao.upsert(entity)
        shareDao.upsertAll(shares)
        return entity
    }

    private fun ExpenseDto.toEntity() = ExpenseEntity(
        id = id,
        groupId = groupId,
        payerId = payerId,
        amountCents = round(amount * 100).toLong(),
        category = category,
        dateMillis = date,
        createdAtMillis = createdAt,
        description = description
    )

    private fun ExpenseShareDto.toEntity() = ExpenseShareEntity(
        id = id,
        expenseId = expenseId,
        userId = userId,
        amountDueCents = round(amountDue * 100).toLong(),
        amountPaidCents = round(amountPaid * 100).toLong(),
        status = status
    )
}