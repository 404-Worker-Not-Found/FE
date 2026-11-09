package com.workernotfound.app.feature.owner.domain.repository

import com.workernotfound.app.feature.owner.domain.model.ApplicantBoard
import com.workernotfound.app.feature.owner.domain.model.ApplicantDetail

/** Owner applicant management (UI spec 2-3). */
interface OwnerApplicantRepository {
    suspend fun proposeMatch(postingId: String, applicantId: String)

    suspend fun getApplicantBoard(postingId: String): ApplicantBoard

    suspend fun getApplicantDetail(postingId: String, applicantId: String): ApplicantDetail
}
