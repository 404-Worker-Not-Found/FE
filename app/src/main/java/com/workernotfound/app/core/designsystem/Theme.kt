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
    val onAccent: Color = AppColors.OnAccent,
    val screenBackground: Color,
)

private val OwnerColors = RoleColors(
    accent = AppColors.Primary,
    accentPressed = AppColors.PrimaryEmphasis,
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

private fun appColorScheme(role: RoleColors) = lightColorScheme(
    primary = role.accentPressed,
    onPrimary = Color.White,
    primaryContainer = role.accentTint,
    onPrimaryContainer = role.accentPressed,
    secondary = role.accentPressed,
    onSecondary = Color.White,
    secondaryContainer = AppColors.SurfaceMuted,
    onSecondaryContainer = AppColors.TextMain,
    tertiary = AppColors.Success,
    onTertiary = Color.White,
    tertiaryContainer = AppColors.SuccessBg,
    onTertiaryContainer = AppColors.Success,
    background = role.screenBackground,
    onBackground = AppColors.TextMain,
    surface = AppColors.CardSurface,
    onSurface = AppColors.TextMain,
    surfaceVariant = AppColors.SurfaceMuted,
    onSurfaceVariant = AppColors.TextSub,
    surfaceContainerLowest = AppColors.CardSurface,
    surfaceContainerLow = role.screenBackground,
    surfaceContainer = AppColors.SurfaceMuted,
    surfaceContainerHigh = AppColors.Border,
    surfaceContainerHighest = AppColors.Border,
    surfaceTint = role.accentPressed,
    outline = AppColors.TextPlaceholder,
    outlineVariant = AppColors.Border,
    error = AppColors.Danger,
    onError = Color.White,
    errorContainer = AppColors.DangerBg,
    onErrorContainer = AppColors.Danger,
    inverseSurface = AppColors.TextMain,
    inverseOnSurface = AppColors.CardSurface,
    inversePrimary = role.accent,
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
            colorScheme = appColorScheme(roleColors),
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
