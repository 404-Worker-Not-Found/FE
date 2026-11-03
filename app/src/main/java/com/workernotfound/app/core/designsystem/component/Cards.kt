package com.workernotfound.app.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppRadius

/**
 * Standard surface card: white fill, 1dp border instead of shadow,
 * 12dp radius; tint is reserved for meaningful emphasis.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    radius: androidx.compose.ui.unit.Dp = AppRadius.card,
    padding: PaddingValues = PaddingValues(AppDimens.cardPadding),
    containerColor: androidx.compose.ui.graphics.Color = AppColors.CardSurface,
    borderColor: androidx.compose.ui.graphics.Color = AppColors.Border,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(radius),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(AppDimens.borderWidth, borderColor),
    ) {
        Column(modifier = Modifier.padding(padding), content = content)
    }
}
