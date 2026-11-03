package com.workernotfound.app.feature.owner.viewmodel

import com.workernotfound.app.feature.owner.domain.NoShowPolicy
import com.workernotfound.app.feature.owner.domain.model.OwnerWork
import com.workernotfound.app.feature.owner.domain.model.WorkProgressStatus

/** Time-derived view of a shift at [nowMillis] (UI spec 2-4: 타이머, 노쇼/근무 완료 버튼 조건). */
data class WorkTiming(val work: OwnerWork, val nowMillis: Long) {
    val isActive: Boolean get() = work.status == WorkProgressStatus.ACTIVE
    val hasStarted: Boolean get() = if (work.backendStatus != null) work.actualStartedMillis != null else nowMillis >= work.scheduledStartMillis
    val elapsedMillis: Long get() = if (work.backendStatus != null && work.actualStartedMillis == null) 0 else (nowMillis - (work.actualStartedMillis ?: work.scheduledStartMillis)).coerceAtLeast(0L)

    /** 노쇼 확정: 근무 시작 30분 초과 + 출근 미인증. */
    val canConfirmNoShow: Boolean
        get() = work.backendStatus == null && isActive && NoShowPolicy.canConfirmNoShow(
            startMillis = work.scheduledStartMillis,
            nowMillis = nowMillis,
            isAttendanceVerified = work.isAttendanceVerified,
        )

    val noShowUnlockRemainingMillis: Long
        get() = NoShowPolicy.millisUntilEnabled(work.scheduledStartMillis, nowMillis)

    /** 근무 완료: only a started shift whose GPS attendance is verified can be completed. */
    val canCompleteWork: Boolean get() = isActive && hasStarted && work.isAttendanceVerified && work.completionAllowed && (work.allowEarlyCompletion || nowMillis >= work.scheduledEndMillis)
}
