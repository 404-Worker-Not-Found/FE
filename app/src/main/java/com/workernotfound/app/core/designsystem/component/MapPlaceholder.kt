package com.workernotfound.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppRadius
import com.workernotfound.app.core.designsystem.AppTheme

/**
 * Placeholder for the Naver map view (decision: Map SDK: Naver Map — wired once
 * the NCP client key is provided). Uses the style guide's custom map colors and
 * shows a store pin in the role accent.
 */
@Composable
fun MapPlaceholder(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 140.dp,
    caption: String = "지도 (Naver 연동 예정)",
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(AppColors.MapBackground, RoundedCornerShape(AppRadius.inner)),
        contentAlignment = Alignment.Center,
    ) {
        // simple "blocks" hint
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 36.dp)
                .background(AppColors.MapBlock, RoundedCornerShape(6.dp))
                .align(Alignment.TopStart),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(AppTheme.role.accent, CircleShape),
            )
            Spacer(Modifier.height(8.dp))
            Text(text = caption, color = AppColors.TextSub, fontSize = 12.sp)
        }
    }
}
