package com.workernotfound.app.feature.worker.ui.component

import androidx.compose.foundation.layout.heightIn
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

/**
 * Compact pill button for instant apply inside a job card (UI spec 3-1: 즉시 지원).
 * Switches to a disabled "지원완료" state once applied.
 */
@Composable
fun WorkerApplyButton(
    status: ApplyStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val applied = status == ApplyStatus.APPLIED
    Button(
        onClick = onClick,
        enabled = !applied,
        modifier = modifier.heightIn(min = com.workernotfound.app.core.designsystem.AppDimens.touchTarget),
        shape = RoundedCornerShape(AppRadius.inner),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.role.accent,
            contentColor = AppTheme.role.onAccent,
            disabledContainerColor = AppColors.SuccessBg,
            disabledContentColor = AppColors.Success,
        ),
    ) {
        Text(
            text = androidx.compose.ui.res.stringResource(if (applied) com.workernotfound.app.R.string.design_applied else com.workernotfound.app.R.string.design_apply),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
