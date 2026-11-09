package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.BuildConfig
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.mypage.data.remote.MemberProfileApiService
import com.workernotfound.app.feature.mypage.data.remote.UpdateMemberNameDto
import com.workernotfound.app.feature.mypage.domain.ProfileNamePolicy
import com.workernotfound.app.feature.worker.domain.model.WorkerProfile
import com.workernotfound.app.feature.worker.domain.repository.WorkerProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkerProfileRepositoryImpl @Inject constructor(
    private val api: MemberProfileApiService,
    private val backend: BackendAccess,
) : WorkerProfileRepository {
    private val demo = DemoWorkerProfileStore()
    private val state = MutableStateFlow<WorkerProfile?>(if (BuildConfig.USE_DEMO_DATA) demo.profile else null)
    override val profile = state.asStateFlow()

    override suspend fun load() {
        if (BuildConfig.USE_DEMO_DATA) { state.value = demo.profile; return }
        state.value = null
        backend.requireSession()
        state.value = api.get().requireData().toWorker()
    }

    override suspend fun updateProfile(profile: WorkerProfile) {
        require(ProfileNamePolicy.isValid(profile.name))
        if (BuildConfig.USE_DEMO_DATA) {
            demo.update(profile)
            state.value = demo.profile
            return
        }
        backend.requireSession()
        val current = checkNotNull(state.value)
        // Only name has a public update contract. Never pretend local settings were saved.
        if (profile != current.copy(name = profile.name)) backend.requireDemo()
        state.value = api.updateName(UpdateMemberNameDto(profile.name)).requireData().toWorker()
    }

    private fun com.workernotfound.app.feature.mypage.data.remote.MemberProfileDto.toWorker(): WorkerProfile {
        if (role != "WORKER") throw ApiException("ROLE_MISMATCH")
        return WorkerProfile(name, email, phoneNumber, null, null, null)
    }
}

/** Explicit demo state, also usable by UI tests without Android/network dependencies. */
class DemoWorkerProfileStore {
    var profile = WorkerProfile("이건운", "leegeonun@example.com", "010-1234-5678", true, false, true)
        private set
    fun update(profile: WorkerProfile) {
        require(ProfileNamePolicy.isValid(profile.name))
        this.profile = profile
    }
}
