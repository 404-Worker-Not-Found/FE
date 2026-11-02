package com.workernotfound.app.core.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.workernotfound.app.core.designsystem.*

/** Changes only presentation; callers continue to own tab selection and navigation. */
@Composable
fun AppAdaptiveScaffold(
    items: List<BottomNavItem>, selectedIndex: Int, onSelect: (Int) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth < AppDimens.navigationBreakpoint) {
            Scaffold(containerColor = AppTheme.role.screenBackground,
                bottomBar = { AppBottomBar(items, selectedIndex, onSelect) }, content = content)
        } else {
            Surface(color = AppTheme.role.screenBackground) {
                Row(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                    NavigationRail(modifier = Modifier.verticalScroll(rememberScrollState()),
                        windowInsets = WindowInsets(0, 0, 0, 0), containerColor = AppColors.CardSurface) {
                        items.forEachIndexed { index, item ->
                            NavigationRailItem(selected = selectedIndex == index,
                                onClick = { onSelect(index) },
                                icon = { Icon(item.icon, contentDescription = null) },
                                label = { Text(item.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = AppTheme.role.accentPressed,
                                    selectedTextColor = AppTheme.role.accentPressed,
                                    indicatorColor = AppTheme.role.accentTint))
                        }
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                        Box(Modifier.widthIn(max = AppDimens.contentMaxWidth).fillMaxSize()) {
                            content(PaddingValues(0.dp))
                        }
                    }
                }
            }
        }
    }
}
