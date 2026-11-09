package com.workernotfound.app.feature.owner.domain.repository

import com.workernotfound.app.feature.owner.domain.model.OwnerJobPosting

/** Owner home data (UI spec 2-1). */
interface OwnerHomeRepository {
    suspend fun getMyPostings(): List<OwnerJobPosting>
}
