package com.workernotfound.app.feature.cancellation.data

import com.workernotfound.app.BuildConfig
import com.workernotfound.app.core.network.*
import com.workernotfound.app.feature.cancellation.domain.*
import com.workernotfound.app.feature.mypage.data.remote.MemberProfileApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerJobApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerWorkApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.*

class CancellationRepositoryImpl @Inject constructor(private val api: CancellationApi, private val backend: BackendAccess,
    private val works: WorkerWorkApiService, private val jobs: WorkerJobApiService,
    private val profiles: MemberProfileApiService) : CancellationRepository {
    private suspend fun requireLive() {
        if (BuildConfig.USE_DEMO_DATA) throw ApiException("LIVE_CANCELLATION_ONLY")
        backend.requireSession()
    }
    override suspend fun context(workId: Long): CancellationContext {
        requireLive()
        val member = profiles.get().requireData().memberId
        val work = works.detail(workId).requireData()
        if (work.workId != workId) throw ApiException("WORK_MISMATCH")
        val job = jobs.detail(work.jobPostId).requireData()
        val policy = works.policy().requireData()
        val startsAt = LocalDate.parse(work.workDate).atTime(LocalTime.parse(work.startTime))
            .atZone(ZoneId.of(policy.timeZone)).toInstant().toEpochMilli()
        val requests = api.list(workId).requireData().map { it.toDomain(workId) }
        return CancellationContext(member, job.storeName, job.description, work.status == "SCHEDULED",
            work.checkedInAt != null, work.confirmedAt != null, startsAt, requests)
    }
    override suspend fun request(workId: Long, key: String, reason: String): Cancellation {
        requireLive()
        require(reason.isNotBlank() && reason.length <= 500)
        return api.request(workId, key, CancellationBody(reason.trim())).requireData().toDomain(workId)
    }
    override suspend fun respond(workId: Long, requestId: Long, action: CancellationAction): Cancellation {
        requireLive()
        val response = when (action) {
            CancellationAction.APPROVE -> api.approve(workId, requestId)
            CancellationAction.REJECT -> api.reject(workId, requestId)
            CancellationAction.WITHDRAW -> api.withdraw(workId, requestId)
            else -> throw IllegalArgumentException("Use request for new cancellations")
        }.requireData()
        val expected = when (action) {
            CancellationAction.APPROVE -> "APPROVED"
            CancellationAction.REJECT -> "REJECTED"
            else -> "WITHDRAWN"
        }
        return response.toDomain(workId, requestId, expected)
    }
}
@Module @InstallIn(SingletonComponent::class)
object CancellationModule {
    @Provides fun api(client: OkHttpClient): CancellationApi = Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL)
        .client(client).addConverterFactory(GsonConverterFactory.create()).build().create(CancellationApi::class.java)
    @Provides fun repository(impl: CancellationRepositoryImpl): CancellationRepository = impl
}
