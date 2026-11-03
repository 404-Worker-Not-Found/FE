package com.workernotfound.app.feature.job.data

import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.job.data.remote.*
import com.workernotfound.app.feature.job.domain.model.*
import com.workernotfound.app.feature.job.domain.repository.JobPublicationRepository
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class JobPublicationRepositoryImpl @Inject constructor(
    private val api: JobPublicationApiService, private val backend: BackendAccess,
) : JobPublicationRepository {
    override suspend fun categories(): List<PostingCategory> = api.categories().requireData().toPostingCategories()

    override suspend fun business(): PostingBusiness {
        backend.requireSession()
        val member = api.me().requireData()
        if (member.role != "OWNER") throw ApiException("OWNER_REQUIRED")
        val profile = member.ownerProfile ?: throw ApiException("BUSINESS_REQUIRED")
        val location = profile.storeLocation
        if (profile.ownerProfileId <= 0) throw ApiException("BUSINESS_REQUIRED")
        // BE OwnerBusinessFindService returns ownerProfileId as businessId.
        return PostingBusiness(profile.ownerProfileId, profile.storeName, location.address, location.latitude, location.longitude)
    }
    override suspend fun create(draft: PublicationDraft): Long {
        backend.requireSession()
        require(draft.isValid(LocalDateTime.now(ZoneId.of("Asia/Seoul"))))
        val id = api.create(draft.toDto()).requireData()
        if (id <= 0) throw ApiException("INVALID_JOB_ID")
        return id
    }
    override suspend fun payment(jobId: Long): PublicationPayment {
        backend.requireSession()
        val job = api.payment(jobId).requireData()
        if (job.jobPostId != jobId) throw ApiException("ORDER_MISMATCH")
        val changePending = job.latestChange?.status == "PENDING"
        val order = if (job.orderCreationStatus == "CREATED" && !changePending) {
            val orderId = job.paymentOrderId?.takeIf { it.isNotBlank() } ?: throw ApiException("ORDER_MISSING")
            api.order(orderId).requireData().also { validateOrder(job, it) }
        } else null
        return PublicationPayment(job.jobPostId, job.jobStatus, job.orderCreationStatus, job.paymentOrderId,
            order?.amount ?: job.amount, order?.currency ?: job.currency, order?.status, changePending)
    }
}

internal fun PublicationDraft.toDto() = CreateJobDto(business.id, categoryId, business.name, business.address,
    title, description, workDate.toString(), start.toString(), end.toString(), nextDay, baseWage, extraWage,
    count, business.latitude, business.longitude, urgency, deadline.toString())

internal fun validateOrder(job: JobPaymentDto, order: PaymentOrderDto) {
    if (order.orderId != job.paymentOrderId || order.jobPostId != job.jobPostId || order.jobVersion != job.paymentJobVersion ||
        job.amount == null || order.amount.compareTo(job.amount) != 0 || order.currency != job.currency || order.currency != "KRW") {
        throw ApiException("ORDER_MISMATCH")
    }
}

/** Preserve actual IDs and hierarchy labels; no guessed seeds or selectable duplicate IDs. */
internal fun JobCategoryListDto.toPostingCategories(): List<PostingCategory> {
    val result = categories.flatMap { parent ->
        if (parent.id <= 0 || parent.name.isBlank()) throw ApiException("INVALID_CATEGORY_CATALOG")
        listOf(PostingCategory(parent.id, parent.name)) + parent.subCategories.map { child ->
            if (child.id <= 0 || child.name.isBlank()) throw ApiException("INVALID_CATEGORY_CATALOG")
            PostingCategory(child.id, "${parent.name} / ${child.name}")
        }
    }
    if (result.map { it.id }.distinct().size != result.size) throw ApiException("INVALID_CATEGORY_CATALOG")
    return result
}
