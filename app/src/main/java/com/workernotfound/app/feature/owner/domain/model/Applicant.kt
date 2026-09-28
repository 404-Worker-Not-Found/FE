package com.workernotfound.app.feature.owner.domain.model

/**
 * An applicant card on the owner applicant list (UI spec 2-3: 지원자 카드).
 * [profileImageUrl] is null in mock data; the UI falls back to an initial avatar.
 */
data class Applicant(
    val id: String,
    val name: String,
    val age: Int,
    val profileImageUrl: String?,
    val rating: Double,
    val etaMinutes: Int,
    val matchScore: Int,
    val noShowRisk: NoShowRiskLevel,
    val hasCategoryExperience: Boolean,
    val trustScore: Int,
    val isMatched: Boolean,
)

/** AI no-show prediction (UI spec 2-3: 노쇼 예측 뱃지 낮음/주의/높음). */
enum class NoShowRiskLevel { LOW, MIDDLE, HIGH }

/** One past shift of an applicant (UI spec 2-3: 근무 이력 — 장소, 날짜, 평점). */
data class ApplicantWorkRecord(
    val place: String,
    val dateText: String,
    val rating: Int,
)

/** Applicant detail (UI spec 2-3: 지원자 상세). */
data class ApplicantDetail(
    val applicant: Applicant,
    val categoryName: String,
    val recentWorks: List<ApplicantWorkRecord>,
    val noShowCount: Int,
    val lastNoShowDate: String?,
    val canConfirmMatch: Boolean,
)

/** A posting together with its applicants (UI spec 2-3: 지원자 목록). */
data class ApplicantBoard(
    val posting: OwnerJobPosting,
    val categoryName: String,
    val applicants: List<Applicant>,
    val matchedApplicantId: String?,
)

/** Maximum number of past shifts shown on the applicant detail (UI spec 2-3: 최근 5건). */
const val RECENT_WORK_LIMIT = 5
