package com.workernotfound.app.core.designsystem

import androidx.compose.ui.graphics.Color

/**
 * Color tokens from docs/agent/style_guide.md.
 *
 * The app is light-mode only (decision: Visual Design System Source of Truth).
 * Do not add dark-mode variants.
 */
object AppColors {
    // Role: Owner (점주) — orange
    val Primary = Color(0xFFFF7E36)
    val PrimaryLight = Color(0xFFFFF0E6)

    // Role: Worker (알바생) — yellow
    val Worker = Color(0xFFFFB703)
    val WorkerEmphasis = Color(0xFFCC8F00)
    val WorkerLight = Color(0xFFFFF8E0)

    // Surfaces
    val Surface = Color(0xFFFAF8F5)        // owner screen background
    val WorkerSurface = Color(0xFFFFFDF5)  // worker screen background
    val CardSurface = Color(0xFFFFFFFF)    // cards / forms
    val Border = Color(0xFFEEEBE6)

    // Text
    val TextMain = Color(0xFF2C2C2C)
    val TextSub = Color(0xFF767676)
    val TextPlaceholder = Color(0xFFBBBBBB)

    // Status
    val Success = Color(0xFF2EBD6B)
    val SuccessBg = Color(0xFFE8F7EF)
    val Warning = Color(0xFFF5A623)
    val WarningBg = Color(0xFFFFF4E0)
    val Danger = Color(0xFFE94B4B)
    val DangerBg = Color(0xFFFDE8E8)

    // Status chip extras (style guide: 상태 배지)
    val WorkingChip = Color(0xFF6366F1)
    val WorkingChipBg = Color(0xFFEEF2FF)

    // Custom map view placeholder (style guide: 지도)
    val MapBackground = Color(0xFFE8E4DC)
    val MapBlock = Color(0xFFD4CFC8)
    val MapPark = Color(0xFFC8DFC8)
    val WorkerLocationDot = Color(0xFF3B82F6)
}
