package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.feature.mypage.domain.ProfileNamePolicy
import com.workernotfound.app.feature.worker.domain.model.WorkerProfile
import com.workernotfound.app.feature.worker.domain.repository.WorkerProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** UI-only profile/settings; persists through navigation for this app session. */
@Singleton
class WorkerProfileRepositoryImpl @Inject constructor() : WorkerProfileRepository {
    private val state = MutableStateFlow(
        WorkerProfile("이건운", "leegeonun@example.com", "010-1234-5678", true, false, true),
    )
    override val profile = state.asStateFlow()

    override fun updateProfile(profile: WorkerProfile) {
        require(ProfileNamePolicy.isValid(profile.name))
        state.value = profile
    }
}
