package com.workernotfound.app.feature.owner.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.component.AppBottomBar
import com.workernotfound.app.core.designsystem.component.BottomNavItem
import com.workernotfound.app.core.designsystem.component.ComingSoonScreen
import com.workernotfound.app.core.navigation.AppRoute

private val OWNER_TABS = listOf(
    BottomNavItem("홈", Icons.Filled.Home),
    BottomNavItem("공고관리", Icons.Filled.Description),
    BottomNavItem("근무관리", Icons.Filled.AccessTime),
    BottomNavItem("마이페이지", Icons.Filled.Person),
)

/**
 * Owner section shell with bottom navigation (UI spec 2-1:
 * 홈 / 공고관리 / 근무관리 / 마이페이지). Tab content switches in place;
 * detail screens are pushed on the app [navController].
 */
@Composable
fun OwnerRootScreen(navController: NavHostController) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = AppColors.Surface,
        bottomBar = {
            AppBottomBar(
                items = OWNER_TABS,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (selectedTab) {
            0 -> OwnerHomeScreen(
                onCreatePosting = { navController.navigate(AppRoute.OWNER_JOB_POSTING) },
                onPostingClick = { navController.navigate(AppRoute.OWNER_APPLICANTS) },
                modifier = contentModifier,
            )
            1 -> ComingSoonScreen(title = "공고 관리 (2-6)", modifier = contentModifier)
            2 -> ComingSoonScreen(title = "근무 관리 (2-4)", modifier = contentModifier)
            else -> ComingSoonScreen(title = "마이페이지", modifier = contentModifier)
        }
    }
}
