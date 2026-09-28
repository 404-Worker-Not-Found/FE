package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.feature.owner.domain.SettlementCalculator
import com.workernotfound.app.feature.owner.domain.model.Applicant
import com.workernotfound.app.feature.owner.domain.model.ApplicantBoard
import com.workernotfound.app.feature.owner.domain.model.ApplicantDetail
import com.workernotfound.app.feature.owner.domain.model.OwnerJobPosting
import com.workernotfound.app.feature.owner.domain.model.OwnerWork
import com.workernotfound.app.feature.owner.domain.model.PastPosting
import com.workernotfound.app.feature.owner.domain.model.PastPostingStatus
import com.workernotfound.app.feature.owner.domain.model.PostingStatus
import com.workernotfound.app.feature.owner.domain.model.RECENT_WORK_LIMIT
import com.workernotfound.app.feature.owner.domain.model.RematchResult
import com.workernotfound.app.feature.owner.domain.model.Settlement
import com.workernotfound.app.feature.owner.domain.model.SettlementRecord
import com.workernotfound.app.feature.owner.domain.model.WorkProgressStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single in-memory source for the owner demo (decision: Demo Scope and Mock-First
 * Strategy). Every owner repository reads and mutates this store so the same
 * posting / worker ids flow home → 지원자 → 근무 → 노쇼 → 지난 공고.
 * Replace with network data sources once the backend contract exists.
 */
@Singleton
class OwnerMockStore @Inject constructor() {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val workers: Map<String, MockWorker> = OwnerMockSeed.workers().associateBy { it.id }
    private val postings: MutableList<MockPosting> = OwnerMockSeed.postings(zone).toMutableList()
    private val works: MutableList<OwnerWork> = OwnerMockSeed.works(postings).toMutableList()
    private val settlements: MutableMap<String, Settlement> = mutableMapOf()
    private val pastPostings: MutableList<PastPosting> =
        OwnerMockSeed.pastPostings(postings, workers).toMutableList()

    // --- 2-1 홈 -------------------------------------------------------------

    @Synchronized
    fun postings(nowMillis: Long): List<OwnerJobPosting> = postings.map { it.toDomain(nowMillis) }

    // --- 2-3 지원자 관리 ------------------------------------------------------

    @Synchronized
    fun applicantBoard(postingId: String, nowMillis: Long): ApplicantBoard {
        val posting = posting(postingId)
        return ApplicantBoard(
            posting = posting.toDomain(nowMillis),
            categoryName = posting.category,
            applicants = posting.applicantIds
                .map { worker(it).toApplicant(posting) }
                .sortedByDescending { it.matchScore },
            matchedApplicantId = posting.matchedWorkerId,
        )
    }

    @Synchronized
    fun applicantDetail(postingId: String, applicantId: String): ApplicantDetail {
        val posting = posting(postingId)
        require(applicantId in posting.applicantIds) { "해당 공고의 지원자가 아니에요." }
        val worker = worker(applicantId)
        return ApplicantDetail(
            applicant = worker.toApplicant(posting),
            categoryName = posting.category,
            recentWorks = worker.recentWorks.take(RECENT_WORK_LIMIT),
            noShowCount = worker.noShowCount,
            lastNoShowDate = worker.lastNoShowDate,
            canConfirmMatch = posting.isMatchable(),
        )
    }

    /** Confirms [applicantId] for [postingId] and opens a shift for it. Returns the work id. */
    @Synchronized
    fun confirmMatch(postingId: String, applicantId: String): String {
        val posting = posting(postingId)
        check(posting.isMatchable()) { "이미 매칭이 확정된 공고예요." }
        require(applicantId in posting.applicantIds) { "해당 공고의 지원자가 아니에요." }
        return openWork(posting, worker(applicantId)).id
    }

    // --- 2-4 근무 관리 / 2-5 노쇼 처리 ------------------------------------------

    @Synchronized
    fun activeWorks(): List<OwnerWork> = works
        .filter { it.status == WorkProgressStatus.ACTIVE || it.status == WorkProgressStatus.AWAITING_SETTLEMENT }
        .sortedBy { it.scheduledStartMillis }

    @Synchronized
    fun work(workId: String): OwnerWork = works.firstOrNull { it.id == workId }
        ?: throw NoSuchElementException("근무 정보를 찾을 수 없어요.")

    @Synchronized
    fun completeWork(workId: String, nowMillis: Long): Settlement {
        val work = work(workId)
        check(work.status == WorkProgressStatus.ACTIVE) { "이미 종료된 근무예요." }
        val minutes = SettlementCalculator.workedMinutes(
            startMillis = work.scheduledStartMillis,
            endMillis = work.scheduledEndMillis,
            completedAtMillis = nowMillis,
        )
        val settlement = SettlementCalculator.calculate(minutes, work.hourlyWage, work.bonusPerHour)
        settlements[workId] = settlement
        replaceWork(work.copy(status = WorkProgressStatus.AWAITING_SETTLEMENT))
        return settlement
    }

    @Synchronized
    fun settlement(workId: String): Settlement = settlements[workId]
        ?: throw NoSuchElementException("정산 내역을 찾을 수 없어요.")

    @Synchronized
    fun confirmSettlement(workId: String, nowMillis: Long) {
        val work = work(workId)
        check(work.status == WorkProgressStatus.AWAITING_SETTLEMENT) { "정산할 근무가 아니에요." }
        val settlement = settlement(workId)
        replaceWork(work.copy(status = WorkProgressStatus.SETTLED))
        updatePosting(work.postingId) { it.copy(status = PostingStatus.DONE) }
        addPastPosting(work, PastPostingStatus.COMPLETED, SettlementRecord(settlement.totalPay, nowMillis))
    }

    @Synchronized
    fun confirmNoShow(workId: String) {
        val work = work(workId)
        check(work.status == WorkProgressStatus.ACTIVE) { "이미 종료된 근무예요." }
        replaceWork(work.copy(status = WorkProgressStatus.NO_SHOW))
        updatePosting(work.postingId) {
            it.copy(matchedWorkerId = null, noShowWorkerIds = it.noShowWorkerIds + work.workerId)
        }
    }

    /** Picks the best remaining applicant of the posting; fails when nobody is left. */
    @Synchronized
    fun rematch(workId: String, nowMillis: Long): RematchResult {
        val noShowWork = work(workId)
        check(noShowWork.status == WorkProgressStatus.NO_SHOW) { "노쇼 확정된 근무가 아니에요." }
        val posting = posting(noShowWork.postingId)
        val candidate = posting.applicantIds
            .filterNot { it in posting.noShowWorkerIds }
            .map { worker(it) }
            .maxByOrNull { it.matchScore }
            ?: return RematchResult.Failure
        // The replacement starts from the moment it is matched; the no-show clock restarts.
        val newWork = openWork(posting, candidate, startMillis = maxOf(nowMillis, posting.startMillis))
        return RematchResult.Success(newWork = newWork, applicant = candidate.toApplicant(posting(posting.id)))
    }

    /** 재매칭 실패 → 공고 재오픈: the posting goes back to recruiting. */
    @Synchronized
    fun reopenPosting(workId: String) {
        updatePosting(work(workId).postingId) { it.copy(status = PostingStatus.RECRUITING, matchedWorkerId = null) }
    }

    /** 재매칭 실패 → 마감: the posting is closed and recorded as a no-show. */
    @Synchronized
    fun closePosting(workId: String) {
        val work = work(workId)
        updatePosting(work.postingId) { it.copy(status = PostingStatus.CLOSED) }
        addPastPosting(work, PastPostingStatus.NO_SHOW, settlement = null)
    }

    // --- 2-6 지난 공고 관리 ------------------------------------------------------

    @Synchronized
    fun pastPostings(): List<PastPosting> = pastPostings.sortedByDescending { it.workStartMillis }

    @Synchronized
    fun pastPosting(postingId: String): PastPosting = pastPostings.firstOrNull { it.postingId == postingId }
        ?: throw NoSuchElementException("지난 공고를 찾을 수 없어요.")

    // --- helpers --------------------------------------------------------------

    private fun posting(postingId: String): MockPosting = postings.firstOrNull { it.id == postingId }
        ?: throw NoSuchElementException("공고를 찾을 수 없어요.")

    private fun worker(workerId: String): MockWorker = workers[workerId]
        ?: throw NoSuchElementException("지원자를 찾을 수 없어요.")

    private fun MockPosting.isMatchable(): Boolean =
        status == PostingStatus.RECRUITING && matchedWorkerId == null

    private fun openWork(
        posting: MockPosting,
        worker: MockWorker,
        startMillis: Long = posting.startMillis,
    ): OwnerWork {
        val work = OwnerWork(
            id = "w${posting.id}-${worker.id}",
            postingId = posting.id,
            workSummary = posting.workSummary,
            workerId = worker.id,
            workerName = worker.name,
            scheduledStartMillis = startMillis,
            scheduledEndMillis = posting.endMillis,
            hourlyWage = posting.hourlyWage,
            bonusPerHour = posting.bonusPerHour,
            isAttendanceVerified = false,
            workerDistanceMeters = worker.etaMinutes * METERS_PER_MINUTE,
            status = WorkProgressStatus.ACTIVE,
        )
        works.removeAll { it.id == work.id }
        works.add(work)
        updatePosting(posting.id) { it.copy(status = PostingStatus.CLOSED, matchedWorkerId = worker.id) }
        return work
    }

    private fun replaceWork(work: OwnerWork) {
        val index = works.indexOfFirst { it.id == work.id }
        if (index >= 0) works[index] = work
    }

    private fun updatePosting(postingId: String, transform: (MockPosting) -> MockPosting) {
        val index = postings.indexOfFirst { it.id == postingId }
        if (index >= 0) postings[index] = transform(postings[index])
    }

    private fun addPastPosting(work: OwnerWork, status: PastPostingStatus, settlement: SettlementRecord?) {
        pastPostings.removeAll { it.postingId == work.postingId }
        pastPostings.add(
            PastPosting(
                postingId = work.postingId,
                workSummary = work.workSummary,
                workerName = work.workerName,
                workStartMillis = work.scheduledStartMillis,
                workEndMillis = work.scheduledEndMillis,
                hourlyWage = work.hourlyWage,
                status = status,
                settlement = settlement,
            ),
        )
    }

    private fun MockWorker.toApplicant(posting: MockPosting) = Applicant(
        id = id,
        name = name,
        age = age,
        profileImageUrl = null,
        rating = rating,
        etaMinutes = etaMinutes,
        matchScore = matchScore,
        noShowRisk = noShowRisk,
        hasCategoryExperience = posting.category in experiencedCategories,
        trustScore = trustScore,
        isMatched = posting.matchedWorkerId == id,
    )

    private fun MockPosting.toDomain(nowMillis: Long) = OwnerJobPosting(
        id = id,
        workSummary = workSummary,
        workDate = relativeDayLabel(startMillis, nowMillis),
        workTime = timeRangeLabel(startMillis, endMillis),
        hourlyWage = hourlyWage,
        status = status,
        applicantCount = applicantIds.size,
        isUrgent = isUrgent,
    )

    private fun relativeDayLabel(millis: Long, nowMillis: Long): String {
        val day = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        return when (val diff = ChronoUnit.DAYS.between(day, today)) {
            0L -> "오늘"
            1L -> "어제"
            -1L -> "내일"
            else -> if (diff > 0) "${diff}일 전" else "${-diff}일 후"
        }
    }

    private fun timeRangeLabel(startMillis: Long, endMillis: Long): String {
        val start = Instant.ofEpochMilli(startMillis).atZone(zone)
        val end = Instant.ofEpochMilli(endMillis).atZone(zone)
        val endLabel = if (end.toLocalTime().toSecondOfDay() == 0 && end.toLocalDate() > start.toLocalDate()) {
            "24:00"
        } else {
            end.format(CLOCK)
        }
        return "${start.format(CLOCK)} ~ $endLabel"
    }

    private companion object {
        val CLOCK: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        /** Rough walking speed used to turn an ETA into a distance for the live map. */
        const val METERS_PER_MINUTE = 70
    }
}
