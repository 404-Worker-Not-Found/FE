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
        modifier = modifier.height(34.dp),
        shape = RoundedCornerShape(AppRadius.pill),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.role.accent,
            contentColor = Color.White,
            disabledContainerColor = AppColors.SuccessBg,
            disabledContentColor = AppColors.Success,
        ),
    ) {
        Text(
            text = if (applied) "지원완료" else "즉시지원",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
