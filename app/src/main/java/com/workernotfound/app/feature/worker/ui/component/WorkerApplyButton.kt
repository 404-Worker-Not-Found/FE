package com.workernotfound.app.feature.worker.ui.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppRadius
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.feature.worker.domain.model.ApplyStatus
import com.workernotfound.app.R
import androidx.compose.ui.res.stringResource

/**
 * Compact pill button for instant apply inside a job card (UI spec 3-1: 즉시 지원).
 * Switches to a disabled "지원완료" state once applied.
 */
@Composable
fun WorkerApplyButton(
    status: ApplyStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isApplying: Boolean = false,
) {
    val blocked = status != ApplyStatus.AVAILABLE
    Button(
        onClick = onClick,
        enabled = !blocked && !isApplying,
        modifier = modifier.height(34.dp),
        shape = RoundedCornerShape(AppRadius.pill),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.role.accent,
            contentColor = Color.White,
            disabledContainerColor = if (status == ApplyStatus.APPLIED) AppColors.SuccessBg else AppColors.Border,
            disabledContentColor = if (status == ApplyStatus.APPLIED) AppColors.Success else AppColors.TextSub,
        ),
    ) {
        Text(
            text = stringResource(when {
                status == ApplyStatus.APPLIED -> R.string.worker_apply_done
                status == ApplyStatus.REAPPLICATION_NOT_ALLOWED -> R.string.worker_apply_again_unavailable
                isApplying -> R.string.worker_apply_progress
                else -> R.string.worker_apply_now
            }),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
