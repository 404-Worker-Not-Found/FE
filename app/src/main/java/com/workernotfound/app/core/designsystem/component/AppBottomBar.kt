package com.workernotfound.app.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppTheme

/** One bottom-navigation entry. */
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
)

/**
 * Bottom navigation bar (style guide: 탭바). White background, top border,
 * active tint = current role accent.
 */
@Composable
fun AppBottomBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = AppTheme.role.accentPressed
    NavigationBar(
        modifier = modifier.fillMaxWidth().drawBehind {
            drawLine(AppColors.Border, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx())
        },
        containerColor = AppColors.CardSurface,
        tonalElevation = 0.dp,
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(index) },
                icon = { Icon(item.icon, contentDescription = null) },
                label = {
                    Text(
                        text = item.label,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = accent,
                    selectedTextColor = accent,
                    indicatorColor = AppTheme.role.accentTint,
                    unselectedIconColor = AppColors.TextSub,
                    unselectedTextColor = AppColors.TextSub,
                ),
            )
        }
    }
}
