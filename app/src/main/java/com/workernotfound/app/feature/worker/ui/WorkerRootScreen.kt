package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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

private val WORKER_TABS = listOf(
    BottomNavItem("홈", Icons.Filled.Home),
    BottomNavItem("탐색", Icons.Filled.Search),
    BottomNavItem("근무관리", Icons.Filled.AccessTime),
    BottomNavItem("채팅", Icons.Filled.ChatBubbleOutline),
    BottomNavItem("마이페이지", Icons.Filled.Person),
)

/**
 * Worker section shell with bottom navigation (UI spec 3-1:
 * 홈 / 탐색 / 근무관리 / 채팅 / 마이페이지). Tab content switches in place;
 * detail screens are pushed on the app [navController].
 */
@Composable
fun WorkerRootScreen(navController: NavHostController) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = AppColors.WorkerSurface,
        bottomBar = {
            AppBottomBar(
                items = WORKER_TABS,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (selectedTab) {
            0 -> WorkerHomeScreen(
                onJobClick = { jobId -> navController.navigate(AppRoute.workerJobDetail(jobId)) },
                modifier = contentModifier,
            )
            1 -> WorkerJobSearchScreen(
                onJobClick = { jobId -> navController.navigate(AppRoute.workerJobDetail(jobId)) },
                modifier = contentModifier,
            )
            2 -> ComingSoonScreen(title = "근무 관리 (3-6)", modifier = contentModifier)
            3 -> ComingSoonScreen(title = "채팅", modifier = contentModifier)
            else -> ComingSoonScreen(title = "마이페이지 (4-2)", modifier = contentModifier)
        }
    }
}
