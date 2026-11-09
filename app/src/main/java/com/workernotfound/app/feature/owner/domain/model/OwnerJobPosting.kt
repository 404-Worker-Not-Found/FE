package com.workernotfound.app.feature.owner.domain.model

/**
 * A posting shown on the owner home dashboard (UI spec 2-1).
 *
 * [workSummary] holds the posting title from the server and is used as the card heading.
 */
data class OwnerJobPosting(
    val id: String,
    val workSummary: String,
    val workDate: String,
    val workTime: String,
    val hourlyWage: Int,
    val status: PostingStatus,
    val applicantCount: Int?,
    val isUrgent: Boolean,
)

enum class PostingStatus { RECRUITING, CLOSED, DONE, PAYMENT_PENDING, MATCHING, FUNDING_BLOCKED }

/** Aggregate counters for the owner home summary strip. */
data class OwnerHomeSummary(
    val activePostingCount: Int,
    val totalApplicantCount: Int?,
)
