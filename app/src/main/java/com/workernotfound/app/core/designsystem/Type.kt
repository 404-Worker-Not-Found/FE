package com.workernotfound.app.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography from docs/agent/style_guide.md.
 *
 * The style guide specifies Pretendard (Korean) / Inter (numbers, latin). Those font
 * files are not bundled yet, so the system default family is used until the font
 * resources are added under res/font and wired here.
 */
private val AppFontFamily = FontFamily.Default

val AppTypography = Typography(
    // 타이틀 (화면 헤더): 18sp Bold
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
    ),
    // 카드 타이틀: 14~15sp Bold
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
    ),
    // 앱 기본: 14sp Regular
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),
    // 보조 텍스트: 12~13sp Regular
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
    ),
    // 섹션 레이블: 11~12sp Bold
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
    ),
)
