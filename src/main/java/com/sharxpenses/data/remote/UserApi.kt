package com.sharxpenses.data.remote

import com.sharxpenses.data.remote.dto.UpdateUserRequest
import com.sharxpenses.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApi {
    @GET("users/me")
    suspend fun me(): UserDto

    @PUT("users/me")
    suspend fun update(@Body body: UpdateUserRequest): UserDto
}