package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import com.workernotfound.app.core.designsystem.component.AppAdaptiveScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.workernotfound.app.core.designsystem.component.BottomNavItem
import com.workernotfound.app.core.navigation.AppRoute

private val WORKER_TABS = listOf(
    BottomNavItem("홈", Icons.Outlined.Home),
    BottomNavItem("탐색", Icons.Outlined.Search),
    BottomNavItem("근무관리", Icons.Outlined.AccessTime),
    BottomNavItem("채팅", Icons.Outlined.ChatBubbleOutline),
    BottomNavItem("마이페이지", Icons.Outlined.Person),
)

/**
 * Worker section shell with bottom navigation (UI spec 3-1:
 * 홈 / 탐색 / 근무관리 / 채팅 / 마이페이지). Tab content switches in place;
 * detail screens are pushed on the app [navController].
 */
@Composable
fun WorkerRootScreen(navController: NavHostController) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    AppAdaptiveScaffold(
        items = WORKER_TABS,
        selectedIndex = selectedTab,
        onSelect = { selectedTab = it },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (selectedTab) {
            0 -> WorkerHomeScreen(
                onJobClick = { jobId -> navController.navigate(AppRoute.workerJobDetail(jobId)) },
                onNotificationsClick = { navController.navigate(AppRoute.WORKER_APPLICATIONS) },
                modifier = contentModifier,
            )
            1 -> WorkerJobSearchScreen(
                onJobClick = { jobId -> navController.navigate(AppRoute.workerJobDetail(jobId)) },
                modifier = contentModifier,
            )
            2 -> WorkerWorkScreen(
                modifier = contentModifier,
                onTrustClick = { navController.navigate(AppRoute.WORKER_TRUST) },
                onApplicationsClick = { navController.navigate(AppRoute.WORKER_APPLICATIONS) },
            )
            3 -> WorkerChatListScreen(
                onRoomClick = { roomId -> navController.navigate(AppRoute.workerChatRoom(roomId)) },
                modifier = contentModifier,
            )
            else -> WorkerMyPageScreen(
                onTrustClick = { navController.navigate(AppRoute.WORKER_TRUST) },
                onLogout = { navController.popBackStack(AppRoute.ROLE_SWITCHER, inclusive = false) },
                modifier = contentModifier,
            )
        }
    }
}
