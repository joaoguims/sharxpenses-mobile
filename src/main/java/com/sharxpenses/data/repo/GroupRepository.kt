package com.sharxpenses.data.repo

import com.sharxpenses.data.local.entity.GroupEntity
import com.sharxpenses.data.local.dao.GroupDao
import com.sharxpenses.data.remote.CreateGroupRequest
import com.sharxpenses.data.remote.GroupApi
import com.sharxpenses.data.remote.GroupDto
import com.sharxpenses.data.local.AppDatabase
import com.sharxpenses.security.TokenStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Retrofit

@Singleton
class GroupRepository @Inject constructor(
    private val db: AppDatabase,
    private val tokenStore: TokenStore,
    private val retrofit: Retrofit,
    private val io: CoroutineDispatcher
) {

    // Aproveita o Retrofit já provido pelo Hilt
    private val api: GroupApi by lazy { retrofit.create(GroupApi::class.java) }
    private val groupDao: GroupDao by lazy { db.groupDao() }

    fun observeGroups(): Flow<List<GroupEntity>> = groupDao.observeGroups()

    suspend fun refresh(): Result<Unit> = withContext(io) {
        runCatching {
            val remote = api.listGroups().map { it.toEntity() }
            groupDao.upsertAll(remote)
        }
    }

    suspend fun create(name: String, description: String?, currency: String = "BRL"): Result<GroupEntity> =
        withContext(io) {
            runCatching {
                val created = api.createGroup(CreateGroupRequest(name, description, currency)).toEntity()
                groupDao.upsert(created)
                created
            }
        }
}

private fun GroupDto.toEntity(): GroupEntity =
    GroupEntity(
        id = id,
        name = name,
        description = description,
        currency = currency,
        createdAt = null // opcional: converter ISO8601 -> epoch millis, se backend enviar
    )