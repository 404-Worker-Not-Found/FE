package com.workernotfound.app.feature.job.domain.repository

import com.workernotfound.app.feature.job.domain.model.*

interface JobPublicationRepository {
    suspend fun categories(): List<PostingCategory>
    suspend fun business(): PostingBusiness
    suspend fun create(draft: PublicationDraft): Long
    suspend fun payment(jobId: Long): PublicationPayment
}
