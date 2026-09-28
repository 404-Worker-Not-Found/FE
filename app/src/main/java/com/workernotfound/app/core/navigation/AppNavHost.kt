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
import com.workernotfound.app.feature.job.ui.JobPostingScreen
import com.workernotfound.app.feature.owner.ui.OwnerApplicantDetailScreen
import com.workernotfound.app.feature.owner.ui.OwnerApplicantsScreen
import com.workernotfound.app.feature.owner.ui.OwnerPastPostingDetailScreen
import com.workernotfound.app.feature.owner.ui.OwnerPastPostingsScreen
import com.workernotfound.app.feature.owner.ui.OwnerRematchScreen
import com.workernotfound.app.feature.owner.ui.OwnerReviewPlaceholderScreen
import com.workernotfound.app.feature.owner.ui.OwnerRootScreen
import com.workernotfound.app.feature.owner.ui.OwnerSettlementScreen
import com.workernotfound.app.feature.owner.ui.OwnerWorkDetailScreen
import com.workernotfound.app.feature.worker.ui.WorkerApplicationScreen
import com.workernotfound.app.feature.worker.ui.WorkerChatRoomScreen
import com.workernotfound.app.feature.worker.ui.WorkerJobDetailScreen
import com.workernotfound.app.feature.worker.ui.WorkerMatchResultScreen
import com.workernotfound.app.feature.worker.ui.WorkerRootScreen
import com.workernotfound.app.feature.worker.ui.WorkerTrustScreen

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
        composable(
            route = AppRoute.OWNER_APPLICANTS,
            arguments = listOf(navArgument("postingId") { type = NavType.StringType }),
        ) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                OwnerApplicantsScreen(
                    onBack = { navController.popBackStack() },
                    onApplicantClick = { postingId, applicantId ->
                        navController.navigate(AppRoute.ownerApplicantDetail(postingId, applicantId))
                    },
                )
            }
        }
        composable(
            route = AppRoute.OWNER_APPLICANT_DETAIL,
            arguments = listOf(
                navArgument("postingId") { type = NavType.StringType },
                navArgument("applicantId") { type = NavType.StringType },
            ),
        ) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                OwnerApplicantDetailScreen(
                    onBack = { navController.popBackStack() },
                    onViewWork = { workId ->
                        navController.navigate(AppRoute.ownerWorkDetail(workId)) {
                            popUpTo(AppRoute.OWNER_ROOT)
                        }
                    },
                )
            }
        }
        composable(
            route = AppRoute.OWNER_WORK_DETAIL,
            arguments = listOf(navArgument("workId") { type = NavType.StringType }),
        ) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                OwnerWorkDetailScreen(
                    onBack = { navController.popBackStack() },
                    onOpenSettlement = { workId -> navController.navigate(AppRoute.ownerSettlement(workId)) },
                    onNoShowConfirmed = { workId ->
                        navController.navigate(AppRoute.ownerRematch(workId)) {
                            popUpTo(AppRoute.OWNER_ROOT)
                        }
                    },
                )
            }
        }
        composable(
            route = AppRoute.OWNER_SETTLEMENT,
            arguments = listOf(navArgument("workId") { type = NavType.StringType }),
        ) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                OwnerSettlementScreen(
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack(AppRoute.OWNER_ROOT, inclusive = false) },
                )
            }
        }
        composable(
            route = AppRoute.OWNER_REMATCH,
            arguments = listOf(navArgument("workId") { type = NavType.StringType }),
        ) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                OwnerRematchScreen(
                    onClose = { navController.popBackStack(AppRoute.OWNER_ROOT, inclusive = false) },
                    onViewWork = { workId ->
                        navController.navigate(AppRoute.ownerWorkDetail(workId)) {
                            popUpTo(AppRoute.OWNER_ROOT)
                        }
                    },
                )
            }
        }
        composable(AppRoute.OWNER_PAST_POSTINGS) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                OwnerPastPostingsScreen(
                    onPostingClick = { postingId -> navController.navigate(AppRoute.ownerPastPostingDetail(postingId)) },
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(
            route = AppRoute.OWNER_PAST_POSTING_DETAIL,
            arguments = listOf(navArgument("postingId") { type = NavType.StringType }),
        ) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                OwnerPastPostingDetailScreen(
                    onBack = { navController.popBackStack() },
                    onWriteReview = { postingId -> navController.navigate(AppRoute.ownerReviewWrite(postingId)) },
                )
            }
        }
        composable(
            route = AppRoute.OWNER_REVIEW_WRITE,
            arguments = listOf(navArgument("postingId") { type = NavType.StringType }),
        ) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                OwnerReviewPlaceholderScreen(onBack = { navController.popBackStack() })
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
        composable(AppRoute.WORKER_APPLICATIONS) {
            WorkerNotFoundTheme(role = AppRole.WORKER) {
                WorkerApplicationScreen(
                    onBack = { navController.popBackStack() },
                    onMatchClick = { id -> navController.navigate(AppRoute.workerMatchResult(id)) },
                )
            }
        }
        composable(
            route = AppRoute.WORKER_MATCH_RESULT,
            arguments = listOf(navArgument("applicationId") { type = NavType.StringType }),
        ) {
            WorkerNotFoundTheme(role = AppRole.WORKER) {
                WorkerMatchResultScreen(
                    onBack = { navController.popBackStack() },
                    onGoChat = { roomId -> navController.navigate(AppRoute.workerChatRoom(roomId)) },
                )
            }
        }
        composable(
            route = AppRoute.WORKER_CHAT_ROOM,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
        ) {
            WorkerNotFoundTheme(role = AppRole.WORKER) {
                WorkerChatRoomScreen(onBack = { navController.popBackStack() })
            }
        }
        composable(AppRoute.WORKER_TRUST) {
            WorkerNotFoundTheme(role = AppRole.WORKER) {
                WorkerTrustScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
