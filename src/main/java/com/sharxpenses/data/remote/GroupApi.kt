package com.sharxpenses.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class GroupDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val currency: String? = "BRL",
    val createdAt: String? = null
)

data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val currency: String? = "BRL"
)

interface GroupApi {
    @GET("groups")
    suspend fun listGroups(): List<GroupDto>

    @POST("groups")
    suspend fun createGroup(@Body body: CreateGroupRequest): GroupDto
}