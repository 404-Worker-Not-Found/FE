package com.workernotfound.app.core.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.workernotfound.app.core.designsystem.AppColors

/** Section label (style guide: 섹션 레이블 — 11~12sp Bold, #BBBBBB). */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = AppColors.TextPlaceholder,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}
