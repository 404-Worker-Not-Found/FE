package com.workernotfound.app.feature.owner.domain.model

/**
 * A posting shown on the owner home dashboard (UI spec 2-1).
 *
 * Note: the posting has no separate title field (UI spec 2-2 defines no title),
 * so [workSummary] is used as the card heading. (decision: Demo Scope — spec wins.)
 */
data class OwnerJobPosting(
    val id: String,
    val workSummary: String,
    val workDate: String,
    val workTime: String,
    val hourlyWage: Int,
    val status: PostingStatus,
    val applicantCount: Int,
    val isUrgent: Boolean,
)

enum class PostingStatus { RECRUITING, CLOSED, DONE }

/** Aggregate counters for the owner home summary strip. */
data class OwnerHomeSummary(
    val activePostingCount: Int,
    val totalApplicantCount: Int,
)
