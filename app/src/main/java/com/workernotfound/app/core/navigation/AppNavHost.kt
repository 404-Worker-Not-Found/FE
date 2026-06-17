package com.workernotfound.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.workernotfound.app.core.designsystem.AppRole
import com.workernotfound.app.core.designsystem.WorkerNotFoundTheme
import com.workernotfound.app.core.designsystem.component.ComingSoonScreen
import com.workernotfound.app.feature.job.ui.JobPostingScreen
import com.workernotfound.app.feature.owner.ui.OwnerRootScreen
import com.workernotfound.app.feature.worker.ui.WorkerJobDetailScreen
import com.workernotfound.app.feature.worker.ui.WorkerRootScreen

/**
 * Top-level navigation. Starts on the demo role switcher, then enters the
 * owner or worker section. Each section re-applies its role theme so the
 * accent color follows the role (style guide: 오렌지=점주, 노랑=알바생).
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = AppRoute.ROLE_SWITCHER) {
        composable(AppRoute.ROLE_SWITCHER) {
            RoleSwitcherScreen(
                onSelectOwner = { navController.navigate(AppRoute.OWNER_ROOT) },
                onSelectWorker = { navController.navigate(AppRoute.WORKER_ROOT) },
            )
        }

        composable(AppRoute.OWNER_ROOT) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                OwnerRootScreen(navController = navController)
            }
        }
        composable(AppRoute.OWNER_JOB_POSTING) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                JobPostingScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack(AppRoute.OWNER_ROOT, inclusive = false) },
                )
            }
        }
        composable(AppRoute.OWNER_APPLICANTS) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                ComingSoonScreen(title = "지원자 관리 (2-3)")
            }
        }
        composable(AppRoute.OWNER_WORK_DETAIL) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                ComingSoonScreen(title = "근무 관리 (2-4)")
            }
        }
        composable(AppRoute.OWNER_PAST_POSTINGS) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                ComingSoonScreen(title = "지난 공고 (2-6)")
            }
        }

        composable(AppRoute.WORKER_ROOT) {
            WorkerNotFoundTheme(role = AppRole.WORKER) {
                WorkerRootScreen(navController = navController)
            }
        }
        composable(
            route = AppRoute.WORKER_JOB_DETAIL,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType }),
        ) {
            WorkerNotFoundTheme(role = AppRole.WORKER) {
                WorkerJobDetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
