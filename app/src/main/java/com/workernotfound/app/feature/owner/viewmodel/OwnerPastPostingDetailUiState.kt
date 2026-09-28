package com.workernotfound.app.feature.owner.viewmodel

import com.workernotfound.app.feature.owner.domain.ReviewPolicy
import com.workernotfound.app.feature.owner.domain.model.PastPosting

/** 지난 공고 상세 상태 (UI spec 2-6). */
data class OwnerPastPostingDetailUiState(
    val isLoading: Boolean = true,
    val posting: PastPosting? = null,
    val nowMillis: Long = System.currentTimeMillis(),
    val errorMessage: String? = null,
) {
    /** 리뷰 작성 버튼: 근무 종료 후 7일 이내만 활성화. */
    val canWriteReview: Boolean
        get() = posting?.let { ReviewPolicy.canWriteReview(it.workEndMillis, nowMillis) } ?: false
}
