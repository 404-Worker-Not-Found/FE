package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.feature.owner.domain.model.OwnerJobPosting
import com.workernotfound.app.feature.owner.domain.model.PostingStatus
import com.workernotfound.app.feature.owner.domain.repository.OwnerHomeRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Mock-backed owner home repository for the demo. In-memory data, with a small
 * delay to exercise the loading state. Replace with a network data source once
 * the backend contract exists; the interface and UI stay unchanged.
 */
class OwnerHomeRepositoryImpl @Inject constructor() : OwnerHomeRepository {

    override suspend fun getMyPostings(): List<OwnerJobPosting> {
        delay(300)
        return MOCK_POSTINGS
    }

    private companion object {
        val MOCK_POSTINGS = listOf(
            OwnerJobPosting(
                id = "1",
                workSummary = "홀서빙 및 주문 응대",
                workDate = "오늘",
                workTime = "18:00 ~ 22:00",
                hourlyWage = 12500,
                status = PostingStatus.RECRUITING,
                applicantCount = 4,
                isUrgent = true,
            ),
            OwnerJobPosting(
                id = "2",
                workSummary = "주방 보조 (설거지/재료 손질)",
                workDate = "오늘",
                workTime = "11:00 ~ 15:00",
                hourlyWage = 11000,
                status = PostingStatus.RECRUITING,
                applicantCount = 2,
                isUrgent = false,
            ),
            OwnerJobPosting(
                id = "3",
                workSummary = "매장 마감 청소",
                workDate = "어제",
                workTime = "22:00 ~ 24:00",
                hourlyWage = 13000,
                status = PostingStatus.DONE,
                applicantCount = 6,
                isUrgent = false,
            ),
            OwnerJobPosting(
                id = "4",
                workSummary = "오픈 준비 및 진열",
                workDate = "2일 전",
                workTime = "07:00 ~ 10:00",
                hourlyWage = 11500,
                status = PostingStatus.CLOSED,
                applicantCount = 1,
                isUrgent = false,
            ),
        )
    }
}
