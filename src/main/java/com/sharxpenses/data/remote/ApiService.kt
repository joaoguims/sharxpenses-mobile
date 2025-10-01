package com.sharxpenses.data.remote

import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiService @Inject constructor(
    private val retrofit: Retrofit
) {
    fun auth(): AuthApi = retrofit.create(AuthApi::class.java)
}
