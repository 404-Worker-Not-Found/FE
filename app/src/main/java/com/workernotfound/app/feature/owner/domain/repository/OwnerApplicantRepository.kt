package com.workernotfound.app.feature.owner.domain.repository

import com.workernotfound.app.feature.owner.domain.model.ApplicantBoard
import com.workernotfound.app.feature.owner.domain.model.ApplicantDetail

/**
 * Owner applicant management (UI spec 2-3). Mock-backed for the demo
 * (decision: Demo Scope and Mock-First Strategy).
 */
interface OwnerApplicantRepository {
    suspend fun getApplicantBoard(postingId: String): ApplicantBoard

    suspend fun getApplicantDetail(postingId: String, applicantId: String): ApplicantDetail

    /** Confirms the match and returns the id of the created shift. */
    suspend fun confirmMatch(postingId: String, applicantId: String): String
}
