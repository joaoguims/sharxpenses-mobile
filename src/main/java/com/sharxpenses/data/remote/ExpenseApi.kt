package com.sharxpenses.data.remote

import retrofit2.http.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

interface ExpenseApi {

    @GET("groups/{id}/expenses")
    suspend fun getExpenses(
        @Path("id") groupId: String
    ): List<ExpenseDto>

    @POST("groups/{id}/expenses")
    suspend fun createExpense(
        @Path("id") groupId: String,
        @Body body: ExpenseReq
    ): ExpenseDto
}

enum class SplitMode { EQUAL, PERCENT, CUSTOM }

@JsonClass(generateAdapter = true)
data class ExpenseReq(
    val amount: Double,
    val category: String,
    val date: Long? = null,
    @Json(name = "payer_id") val payerId: String,
    val participants: List<String>,
    val splitMode: SplitMode,
    val customShares: List<CustomShare>? = null,
    val percents: List<PercentShare>? = null,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class CustomShare(
    val userId: String,
    val amount: Double
)

@JsonClass(generateAdapter = true)
data class PercentShare(
    val userId: String,
    val percent: Double
)

@JsonClass(generateAdapter = true)
data class ExpenseDto(
    val id: String,
    val groupId: String,
    val payerId: String,
    val amount: Double,
    val category: String,
    val date: Long,
    val createdAt: Long,
    val shares: List<ExpenseShareDto> = emptyList(),
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class ExpenseShareDto(
    val id: String,
    val expenseId: String,
    val userId: String,
    val amountDue: Double,
    val amountPaid: Double = 0.0,
    val status: String = "OPEN"
)