package com.sharxpenses.data.remote

import com.sharxpenses.data.remote.dto.DeviceTokenRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface DevicesApi {
    @POST("devices")
    suspend fun registerDevice(@Body body: DeviceTokenRequest)
}