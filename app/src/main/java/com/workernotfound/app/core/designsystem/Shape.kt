package com.workernotfound.app.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape & radius tokens from docs/agent/style_guide.md.
 * 둥글고 따뜻한 느낌. 직각 없음.
 */
object AppRadius {
    val card = 16.dp        // 일반 카드
    val inner = 12.dp       // 카드 내부 요소
    val bottomSheet = 24.dp // 바텀시트
    val button = 16.dp      // 일반 버튼
    val pill = 999.dp       // pill / chip
}

val AppShapes = Shapes(
    small = RoundedCornerShape(AppRadius.inner),
    medium = RoundedCornerShape(AppRadius.card),
    large = RoundedCornerShape(AppRadius.bottomSheet),
)
