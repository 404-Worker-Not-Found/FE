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
 * Top-level navigation. Starts with onboarding/authentication, then enters the
 * owner or worker section. Each section re-applies its role theme so the
 * accent color follows the role (style guide: 오렌지=점주, 노랑=알바생).
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = AppRoute.ROLE_SWITCHER) {
        composable(AppRoute.ROLE_SWITCHER) {
            com.workernotfound.app.feature.onboarding.StartupGate {
                com.workernotfound.app.feature.auth.ui.AuthScreen(onAuthenticated = { session ->
                    val route = if (session.role == "OWNER") AppRoute.OWNER_ROOT else AppRoute.WORKER_ROOT
                    navController.navigate(route) {
                        popUpTo(AppRoute.ROLE_SWITCHER)
                        launchSingleTop = true
                    }
                })
            }
        }

        composable(AppRoute.OWNER_ROOT) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                OwnerRootScreen(navController = navController)
            }
        }
        composable(AppRoute.CHAT_JOB_DETAIL, arguments = listOf(navArgument("jobId") { type = NavType.StringType })) {
            com.workernotfound.app.feature.worker.ui.ChatPostingScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoute.OWNER_CHATS) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                com.workernotfound.app.feature.worker.ui.WorkerChatListScreen(
                    onRoomClick = { navController.navigate(AppRoute.ownerChatRoom(it)) },
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(AppRoute.OWNER_CHAT_ROOM, arguments = listOf(navArgument("roomId") { type = NavType.StringType })) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                com.workernotfound.app.feature.worker.ui.WorkerChatRoomScreen(onBack = { navController.popBackStack() },
                    onJobClick = { navController.navigate(AppRoute.chatJobDetail(it)) })
            }
        }
        listOf(AppRoute.OWNER_CANCELLATION to AppRole.OWNER, AppRoute.WORKER_CANCELLATION to AppRole.WORKER).forEach { (route, role) ->
            composable(route, arguments = listOf(navArgument("workId") { type = NavType.StringType })) {
                WorkerNotFoundTheme(role = role) {
                    com.workernotfound.app.feature.cancellation.ui.CancellationScreen(onBack = { navController.popBackStack() })
                }
            }
        }
        listOf(AppRoute.OWNER_REVIEWS to AppRole.OWNER, AppRoute.WORKER_REVIEWS to AppRole.WORKER).forEach { (route, role) ->
            composable(route) {
                WorkerNotFoundTheme(role = role) {
                    com.workernotfound.app.feature.review.ui.ReviewScreen(onBack = { navController.popBackStack() })
                }
            }
        }
        listOf(AppRoute.OWNER_ACCOUNT to AppRole.OWNER, AppRoute.WORKER_ACCOUNT to AppRole.WORKER).forEach { (route, role) ->
            composable(route) {
                WorkerNotFoundTheme(role = role) {
                    com.workernotfound.app.feature.account.ui.AccountScreen(
                        onBack = { navController.popBackStack() },
                        onLogin = { navController.navigate(AppRoute.ROLE_SWITCHER) {
                            popUpTo(AppRoute.ROLE_SWITCHER) { inclusive = true }; launchSingleTop = true
                        } },
                    )
                }
            }
        }
        composable(AppRoute.WORKER_NOTIFICATIONS) {
            WorkerNotFoundTheme(role = AppRole.WORKER) {
                com.workernotfound.app.feature.notification.ui.NotificationScreen(
                    onBack = { navController.popBackStack() },
                    onOpen = { item -> navController.navigate(if (item.applicationId != null)
                        AppRoute.workerMatchResult(item.applicationId.toString()) else AppRoute.workerJobDetail(item.jobId.toString())) },
                )
            }
        }
        composable(AppRoute.OWNER_NOTIFICATIONS) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                com.workernotfound.app.feature.notification.ui.NotificationScreen(
                    onBack = { navController.popBackStack() },
                    onOpen = { item -> navController.navigate(if (item.workId != null)
                        AppRoute.ownerWorkDetail(item.workId.toString()) else AppRoute.ownerApplicants(item.jobId.toString())) },
                )
            }
        }
        composable(
            route = AppRoute.OWNER_PUBLICATION,
            arguments = listOf(navArgument("publicationJobId") { type = NavType.LongType }),
        ) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                com.workernotfound.app.feature.job.ui.JobPublicationScreen(onBack = { navController.popBackStack() })
            }
        }
        composable(AppRoute.OWNER_JOB_POSTING) {
            WorkerNotFoundTheme(role = AppRole.OWNER) {
                JobPostingScreen(
                    onBack = { navController.popBackStack() },
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
                    onCancellation = { id -> navController.navigate(AppRoute.ownerCancellation(id)) },
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
                    onWriteReview = { navController.navigate(AppRoute.OWNER_REVIEWS) },
                )
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
                    onHome = { navController.popBackStack(AppRoute.WORKER_ROOT, inclusive = false) },
                )
            }
        }
        composable(
            route = AppRoute.WORKER_CHAT_ROOM,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
        ) {
            WorkerNotFoundTheme(role = AppRole.WORKER) {
                WorkerChatRoomScreen(onBack = { navController.popBackStack() },
                    onJobClick = { navController.navigate(AppRoute.chatJobDetail(it)) })
            }
        }
        composable(AppRoute.WORKER_HISTORY) {
            WorkerNotFoundTheme(role = AppRole.WORKER) {
                com.workernotfound.app.feature.history.ui.WorkHistoryScreen(
                    onBack = { navController.popBackStack() },
                    onJob = { navController.navigate(AppRoute.workerJobDetail(it)) },
                    onReviews = { navController.navigate(AppRoute.WORKER_REVIEWS) })
            }
        }
        composable(AppRoute.WORKER_TRUST) {
            WorkerNotFoundTheme(role = AppRole.WORKER) {
                WorkerTrustScreen(
                    onBack = { navController.popBackStack() },
                    onReviews = { navController.navigate(AppRoute.WORKER_REVIEWS) },
                    onHistory = { navController.navigate(AppRoute.WORKER_HISTORY) },
                )
            }
        }
    }
}
