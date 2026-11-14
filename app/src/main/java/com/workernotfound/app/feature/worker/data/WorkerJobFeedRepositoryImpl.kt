package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.core.location.DeviceLocationProvider
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.mypage.data.remote.MemberProfileApiService
import com.workernotfound.app.feature.mypage.data.remote.MemberProfileDto
import com.workernotfound.app.feature.worker.domain.model.AvailableTime
import com.workernotfound.app.feature.worker.domain.model.JobFilter
import com.workernotfound.app.feature.worker.domain.model.ApplyStatus
import com.workernotfound.app.feature.worker.domain.model.WorkerPreference
import com.workernotfound.app.feature.worker.domain.repository.SearchOrigin
import com.workernotfound.app.feature.worker.domain.repository.WorkerJobFeed
import com.workernotfound.app.feature.worker.domain.repository.WorkerJobFeedRepository
import com.workernotfound.app.feature.worker.domain.repository.WorkerJobRepository
import com.workernotfound.app.feature.worker.domain.repository.WorkerApplicationRepository
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import com.workernotfound.app.core.datastore.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
class WorkerJobFeedRepositoryImpl @Inject constructor(
    private val jobs: WorkerJobRepository,
    private val members: MemberProfileApiService,
    private val positions: DeviceLocationProvider,
    private val backend: BackendAccess,
    private val sessions: SessionStore,
    private val applications: WorkerApplicationRepository,
) : WorkerJobFeedRepository {
    private val lock = Mutex()
    private var cached: Pair<Long, WorkerPreference>? = null

    override suspend fun preference(refresh: Boolean): WorkerPreference = lock.withLock {
        // Keyed by member so a different login never sees the previous worker's preferences.
        val memberId = withContext(Dispatchers.IO) { sessions.read()?.memberId }
        cached?.takeIf { !refresh && it.first == memberId }?.let { return it.second }
        backend.requireSession()
        val loaded = members.get().requireData()
        loaded.toPreference().also { cached = loaded.memberId to it }
    }

    override suspend fun load(filter: JobFilter): WorkerJobFeed {
        val preference = preference()
        val device = if (filter.latitude == null) positions.lastKnownOrNull() else null
        val (origin, located) = when {
            filter.latitude != null && filter.longitude != null -> SearchOrigin.FILTER to filter
            device != null -> SearchOrigin.DEVICE to filter.copy(latitude = device.latitude, longitude = device.longitude)
            preference.hasBaseLocation -> SearchOrigin.BASE_LOCATION to
                filter.copy(latitude = preference.baseLatitude, longitude = preference.baseLongitude)
            else -> SearchOrigin.NONE to filter
        }
        val result = jobs.searchJobs(located)
        val applyStatuses = applications.applyStatusesByJobId()
        val postings = result.jobs.map { job ->
            job.copy(applyStatus = applyStatuses[job.id]?.status ?: ApplyStatus.AVAILABLE)
        }
        return WorkerJobFeed(preference, origin, located.latitude, located.longitude, postings)
    }
}

internal fun MemberProfileDto.toPreference(): WorkerPreference {
    if (role != "WORKER") throw ApiException("ROLE_MISMATCH")
    val worker = workerProfile
    return WorkerPreference(
        name = name,
        desiredHourlyWage = worker?.desiredHourlyWage?.takeIf { it > 0 },
        activityRadiusKm = worker?.activityRadiusKm?.takeIf { it > 0 },
        baseLatitude = worker?.baseLocation?.latitude,
        baseLongitude = worker?.baseLocation?.longitude,
        preferredBusinessTypes = worker?.preferredBusinessTypes.orEmpty().filter { it.isNotBlank() },
        availableTimes = worker?.availableTimes.orEmpty().mapNotNull { slot ->
            val day = runCatching { DayOfWeek.valueOf(slot.dayOfWeek.orEmpty()) }.getOrNull()
            val start = runCatching { LocalTime.parse(slot.startTime.orEmpty()) }.getOrNull()
            val end = runCatching { LocalTime.parse(slot.endTime.orEmpty()) }.getOrNull()
            if (day != null && start != null && end != null) AvailableTime(day, start, end) else null
        },
    )
}
