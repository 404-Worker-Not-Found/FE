package com.workernotfound.app.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppRadius
import com.workernotfound.app.core.designsystem.AppTheme

/**
 * Primary CTA button. Defaults to the current role accent
 * (orange for owner, yellow for worker — style guide: CTA 버튼).
 */
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    pill: Boolean = false,
) {
    val accent = AppTheme.role.accent
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(if (pill) AppRadius.pill else AppRadius.button),
        colors = ButtonDefaults.buttonColors(
            containerColor = accent,
            contentColor = Color.White,
            disabledContainerColor = AppColors.Border,
            disabledContentColor = AppColors.TextPlaceholder,
        ),
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

/** Full-width outlined secondary action (e.g. 다시 시도, 거절, 홈으로) — pairs with [AppPrimaryButton]. */
@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = AppTheme.role.accentPressed,
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(AppRadius.button),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (enabled) color else AppColors.Border),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color, disabledContentColor = AppColors.TextPlaceholder),
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

/** Compact outlined pill for an action inside a card (e.g. 지원 취소, 위치 확인). */
@Composable
fun AppSmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = AppTheme.role.accentPressed,
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(AppRadius.pill),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (enabled) color else AppColors.Border),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color, disabledContentColor = AppColors.TextPlaceholder),
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = androidx.compose.ui.unit.TextUnit(13f, androidx.compose.ui.unit.TextUnitType.Sp))
    }
}
