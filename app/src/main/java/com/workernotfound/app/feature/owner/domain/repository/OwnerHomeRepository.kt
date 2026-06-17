package com.workernotfound.app.feature.owner.domain.repository

import com.workernotfound.app.feature.owner.domain.model.OwnerJobPosting

/**
 * Owner home data (UI spec 2-1). Backed by a mock source for the demo;
 * swap the implementation for a network-backed one when the backend contract
 * is confirmed (decision: Demo Scope and Mock-First Strategy).
 */
interface OwnerHomeRepository {
    suspend fun getMyPostings(): List<OwnerJobPosting>
}
