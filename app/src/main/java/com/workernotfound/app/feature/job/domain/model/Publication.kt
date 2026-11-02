package com.workernotfound.app.feature.job.domain.model

import java.math.BigDecimal
import java.time.*

data class PostingCategory(val id: Long, val name: String)

data class PostingBusiness(val id: Long, val name: String, val address: String, val latitude: BigDecimal, val longitude: BigDecimal)
data class PublicationDraft(
    val business: PostingBusiness, val categoryId: Long, val title: String, val description: String,
    val workDate: LocalDate, val start: LocalTime, val end: LocalTime, val nextDay: Boolean,
    val baseWage: Int, val extraWage: Int?, val count: Int, val urgency: String, val deadline: LocalDateTime,
) {
    fun isValid(now: LocalDateTime): Boolean {
        val minutes = Duration.between(start, end).toMinutes() + if (nextDay) 1440 else 0
        return business.id > 0 && business.address.isNotBlank() &&
            business.latitude >= BigDecimal("-90") && business.latitude <= BigDecimal("90") &&
            business.longitude >= BigDecimal("-180") && business.longitude <= BigDecimal("180") && categoryId > 0 && title.isNotBlank() && description.isNotBlank() &&
            description.length <= 200 && baseWage >= MIN_HOURLY_WAGE && (extraWage == null || extraWage > 0) &&
            count in 1..10 && urgency in setOf("LOW", "MEDIUM", "HIGH") && minutes in 1..1440 &&
            start.second == 0 && end.second == 0 && start.nano == 0 && end.nano == 0 &&
            workDate in now.toLocalDate()..now.toLocalDate().plusDays(1) && deadline > now && deadline < workDate.atTime(start)
    }
}

data class PublicationPayment(
    val jobId: Long, val jobStatus: String, val creationStatus: String,
    val orderId: String?, val amount: BigDecimal?, val currency: String?, val orderStatus: String?,
    val changePending: Boolean,
) {
    // Payment confirmation and publication are separate server transitions.
    val isPublished get() = jobStatus == "OPEN"
}
