package com.workernotfound.app.feature.owner.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppRadius

/**
 * Outlined secondary button. [color] defaults to the danger red used for
 * 노쇼 actions (style guide: Danger = 노쇼, 오류, 위험).
 */
@Composable
internal fun OwnerOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = AppColors.Danger,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(AppRadius.button),
        border = BorderStroke(AppDimens.borderWidth, if (enabled) color else AppColors.Border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AppColors.CardSurface,
            contentColor = color,
            disabledContainerColor = AppColors.CardSurface,
            disabledContentColor = AppColors.TextPlaceholder,
        ),
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}
