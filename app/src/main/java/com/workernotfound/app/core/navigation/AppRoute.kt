package com.workernotfound.app.core.navigation

/** App-level navigation routes. Detail routes are added as screens are built. */
object AppRoute {
    const val ROLE_SWITCHER = "role_switcher"

    // Owner section
    const val OWNER_ROOT = "owner_root"
    const val OWNER_JOB_POSTING = "owner_job_posting"        // 2-2 공고 등록
    const val OWNER_APPLICANTS = "owner_applicants/{postingId}"  // 2-3 지원자 관리
    fun ownerApplicants(postingId: String) = "owner_applicants/$postingId"

    const val OWNER_APPLICANT_DETAIL = "owner_applicant_detail/{postingId}/{applicantId}"  // 2-3 지원자 상세
    fun ownerApplicantDetail(postingId: String, applicantId: String) =
        "owner_applicant_detail/$postingId/$applicantId"

    const val OWNER_WORK_DETAIL = "owner_work_detail/{workId}"  // 2-4 근무 관리/정산
    fun ownerWorkDetail(workId: String) = "owner_work_detail/$workId"

    const val OWNER_SETTLEMENT = "owner_settlement/{workId}"   // 2-4 정산
    fun ownerSettlement(workId: String) = "owner_settlement/$workId"

    const val OWNER_PAST_POSTINGS = "owner_past_postings"    // 2-6 지난 공고

    // Worker section
    const val WORKER_ROOT = "worker_root"
    const val WORKER_JOB_DETAIL = "worker_job_detail/{jobId}"   // 3-3 공고 상세
    fun workerJobDetail(jobId: String) = "worker_job_detail/$jobId"

    const val WORKER_APPLICATIONS = "worker_applications"        // 3-4 지원 현황
    const val WORKER_MATCH_RESULT = "worker_match_result/{applicationId}"  // 3-5 매칭 결과
    fun workerMatchResult(applicationId: String) = "worker_match_result/$applicationId"

    const val WORKER_CHAT_ROOM = "worker_chat_room/{roomId}"     // 채팅방
    fun workerChatRoom(roomId: String) = "worker_chat_room/$roomId"

    const val WORKER_TRUST = "worker_trust"                      // 4-2 나의 신뢰도
}
