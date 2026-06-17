package com.workernotfound.app.feature.worker.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.UrgentBadge
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting

/** Wage text in the worker accent-emphasis color (style guide: 금액 강조 #CC8F00). */
@Composable
private fun WageText(wage: Int, modifier: Modifier = Modifier) {
    Text(
        text = "₩${"%,d".format(wage)}",
        color = AppTheme.role.accentPressed,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}

/**
 * Urgent opening card (UI spec 3-1: 긴급 공고). Shown in a horizontal row, so it
 * has a fixed width. Highlights the urgency badge and remaining time.
 */
@Composable
fun WorkerUrgentJobCard(
    job: WorkerJobPosting,
    onClick: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.width(240.dp).clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UrgentBadge()
            Spacer(Modifier.weight(1f))
            job.deadlineText?.let {
                Text(text = it, color = AppColors.Danger, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(text = job.storeName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${job.category} · ${job.workTime}",
            color = AppColors.TextSub,
            fontSize = 12.sp,
        )
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column {
                WageText(job.hourlyWage)
                Text(text = job.distanceText, color = AppColors.TextSub, fontSize = 11.sp)
            }
            Spacer(Modifier.weight(1f))
            WorkerApplyButton(status = job.applyStatus, onClick = onApply)
        }
    }
}

/**
 * Recommended opening card (UI spec 3-1: 추천 공고). Shown in a vertical list, so
 * it fills width and lays out in a single row.
 */
@Composable
fun WorkerRecommendedJobCard(
    job: WorkerJobPosting,
    onClick: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = job.storeName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${job.category} · ${job.workTime} · ${job.distanceText}",
                    color = AppColors.TextSub,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(8.dp))
                WageText(job.hourlyWage)
            }
            Spacer(Modifier.width(12.dp))
            WorkerApplyButton(status = job.applyStatus, onClick = onApply)
        }
    }
}
