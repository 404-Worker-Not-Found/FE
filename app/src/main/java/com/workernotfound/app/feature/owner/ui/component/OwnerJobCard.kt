package com.workernotfound.app.feature.owner.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
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
import com.workernotfound.app.core.designsystem.component.AppStatus
import com.workernotfound.app.core.designsystem.component.StatusBadge
import com.workernotfound.app.core.designsystem.component.UrgentBadge
import com.workernotfound.app.feature.owner.domain.model.OwnerJobPosting
import com.workernotfound.app.feature.owner.domain.model.PostingStatus

@Composable
fun OwnerJobCard(
    posting: OwnerJobPosting,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (posting.isUrgent) {
                UrgentBadge()
                Spacer(Modifier.width(6.dp))
            }
            StatusBadge(status = posting.status.toBadge())
            Spacer(Modifier.weight(1f))
            Text(
                text = "지원자 ${posting.applicantCount}명",
                color = AppColors.TextSub,
                fontSize = 12.sp,
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = posting.workSummary,
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.TextMain,
        )

        Spacer(Modifier.height(6.dp))
        Text(
            text = "${posting.workDate} · ${posting.workTime}",
            color = AppColors.TextSub,
            fontSize = 13.sp,
        )

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                text = "시급 ${"%,d".format(posting.hourlyWage)}원",
                color = AppTheme.role.accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun PostingStatus.toBadge(): AppStatus = when (this) {
    PostingStatus.RECRUITING -> AppStatus.RECRUITING
    PostingStatus.CLOSED -> AppStatus.CLOSED
    PostingStatus.DONE -> AppStatus.DONE
}
