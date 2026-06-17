package com.workernotfound.app.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The two product roles. Drives the accent color and screen background
 * (style guide: 오렌지=점주, 노랑=알바생).
 */
enum class AppRole { OWNER, WORKER }

/** Role-derived accent values resolved from [AppColors]. */
data class RoleColors(
    val accent: Color,
    val accentPressed: Color,
    val accentTint: Color,
    val screenBackground: Color,
)

private val OwnerColors = RoleColors(
    accent = AppColors.Primary,
    accentPressed = AppColors.Primary,
    accentTint = AppColors.PrimaryLight,
    screenBackground = AppColors.Surface,
)

private val WorkerColors = RoleColors(
    accent = AppColors.Worker,
    accentPressed = AppColors.WorkerEmphasis,
    accentTint = AppColors.WorkerLight,
    screenBackground = AppColors.WorkerSurface,
)

val LocalRoleColors = staticCompositionLocalOf { OwnerColors }

/** Convenience accessor: `AppTheme.role.accent`. */
object AppTheme {
    val role: RoleColors
        @Composable get() = LocalRoleColors.current
}

private val AppColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    background = AppColors.Surface,
    onBackground = AppColors.TextMain,
    surface = AppColors.CardSurface,
    onSurface = AppColors.TextMain,
    surfaceVariant = AppColors.PrimaryLight,
    outline = AppColors.Border,
    error = AppColors.Danger,
)

@Composable
fun WorkerNotFoundTheme(
    role: AppRole = AppRole.OWNER,
    content: @Composable () -> Unit,
) {
    val roleColors = when (role) {
        AppRole.OWNER -> OwnerColors
        AppRole.WORKER -> WorkerColors
    }
    CompositionLocalProvider(LocalRoleColors provides roleColors) {
        MaterialTheme(
            colorScheme = AppColorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
