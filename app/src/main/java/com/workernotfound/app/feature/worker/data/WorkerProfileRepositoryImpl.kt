package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.mypage.data.remote.MemberProfileApiService
import com.workernotfound.app.feature.mypage.data.remote.MemberProfileDto
import com.workernotfound.app.feature.mypage.data.remote.UpdateMemberNameDto
import com.workernotfound.app.feature.mypage.domain.ProfileNamePolicy
import com.workernotfound.app.feature.worker.domain.model.WorkerProfile
import com.workernotfound.app.feature.worker.domain.repository.WorkerProfileRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class WorkerProfileRepositoryImpl @Inject constructor(
    private val api: MemberProfileApiService,
    private val backend: BackendAccess,
) : WorkerProfileRepository {
    private val state = MutableStateFlow<WorkerProfile?>(null)
    override val profile = state.asStateFlow()

    override suspend fun load() {
        state.value = null
        backend.requireSession()
        state.value = api.get().requireData().toWorker()
    }

    override suspend fun updateProfile(profile: WorkerProfile) {
        require(ProfileNamePolicy.isValid(profile.name))
        backend.requireSession()
        val current = checkNotNull(state.value)
        // Only name has a public update contract. Never pretend local settings were saved.
        if (profile != current.copy(name = profile.name)) backend.unsupported()
        state.value = api.updateName(UpdateMemberNameDto(profile.name)).requireData().toWorker()
    }

    private fun MemberProfileDto.toWorker(): WorkerProfile {
        if (role != "WORKER") throw ApiException("ROLE_MISMATCH")
        return WorkerProfile(name, email, phoneNumber, null, null, null)
    }
}
