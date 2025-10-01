package com.sharxpenses.di

import android.util.Log
import com.sharxpenses.data.remote.dto.AuthResponse
import com.sharxpenses.data.remote.dto.RefreshRequest
import com.sharxpenses.security.TokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Interceptor que adiciona Authorization: Bearer <access>
 * e, em caso de 401, tenta 1x fazer refresh com o refreshToken salvo.
 * Observação: usa um Retrofit separado (sem interceptor) apenas para /auth/refresh.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
    @Named(\"refreshRetrofit\") private val refreshRetrofit: Retrofit
) : Interceptor {

    private val refreshApi by lazy { refreshRetrofit.create(RefreshApi::class.java) }

    @Volatile private var isRefreshing = false

    override fun intercept(chain: Interceptor.Chain): Response {
        var req = chain.request()
        val access = tokenStore.getAccessToken()
        if (!access.isNullOrBlank()) {
            req = req.newBuilder()
                .addHeader(\"Authorization\", \"Bearer \\")
                .build()
        }

        val originalRes = chain.proceed(req)
        if (originalRes.code != 401) return originalRes

        originalRes.close()

        val refresh = tokenStore.getRefreshToken()
        if (refresh.isNullOrBlank()) return chain.proceed(req) // sem refresh, retorna 401

        synchronized(this) {
            if (!isRefreshing) {
                isRefreshing = true
                try {
                    val call = refreshApi.refresh(RefreshRequest(refresh))
                    val resp = call.execute()
                    if (resp.isSuccessful) {
                        val body = resp.body()
                        if (body != null && !body.accessToken.isNullOrBlank()) {
                            tokenStore.setAccessToken(body.accessToken)
                        } else {
                            tokenStore.clear()
                        }
                    } else {
                        tokenStore.clear()
                    }
                } catch (t: Throwable) {
                    Log.e(\"AuthInterceptor\", \"Refresh error\", t)
                    tokenStore.clear()
                } finally {
                    isRefreshing = false
                }
            }
        }

        val newAccess = tokenStore.getAccessToken()
        val retried = if (!newAccess.isNullOrBlank()) {
            chain.request().newBuilder()
                .removeHeader(\"Authorization\")
                .addHeader(\"Authorization\", \"Bearer \\")
                .build()
        } else {
            req
        }
        return chain.proceed(retried)
    }

    private interface RefreshApi {
        @POST(\"auth/refresh\")
        fun refresh(@Body body: RefreshRequest): Call<AuthResponse>
    }
}

@Module
@InstallIn(SingletonComponent::class)
object InterceptorsModule {

    // Base URL de fallback (deve casar com a do NetworkModule)
    private const val BASE_URL = \"http://10.0.2.2:8080/\"

    /**
     * Retrofit específico para /auth/refresh (sem AuthInterceptor)
     */
    @Provides
    @Singleton
    @Named(\"refreshRetrofit\")
    fun provideRefreshRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()
    }

    /**
     * Fornece o AuthInterceptor
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenStore: TokenStore,
        @Named(\"refreshRetrofit\") refreshRetrofit: Retrofit
    ): AuthInterceptor = AuthInterceptor(tokenStore, refreshRetrofit)

    /**
     * Cliente e Retrofit \"authed\" (com AuthInterceptor).
     * Use este Retrofit para APIs que exigem autorização.
     */
    @Provides
    @Singleton
    @Named(\"okhttpAuthed\")
    fun provideOkHttpAuthed(
        authInterceptor: AuthInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @Named(\"retrofitAuthed\")
    fun provideRetrofitAuthed(
        @Named(\"okhttpAuthed\") client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .client(client)
        .build()
}