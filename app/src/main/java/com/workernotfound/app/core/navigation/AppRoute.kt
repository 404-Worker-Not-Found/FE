package com.workernotfound.app.core.navigation

/** App-level navigation routes. Detail routes are added as screens are built. */
object AppRoute {
    const val ROLE_SWITCHER = "role_switcher"

    // Owner section
    const val OWNER_ROOT = "owner_root"
    const val OWNER_JOB_POSTING = "owner_job_posting"        // 2-2 공고 등록
    const val OWNER_APPLICANTS = "owner_applicants"          // 2-3 지원자 관리
    const val OWNER_WORK_DETAIL = "owner_work_detail"        // 2-4 근무 관리/정산
    const val OWNER_PAST_POSTINGS = "owner_past_postings"    // 2-6 지난 공고

    // Worker section (built after the owner flow)
    const val WORKER_ROOT = "worker_root"
}
