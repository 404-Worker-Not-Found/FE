package com.workernotfound.app.feature.owner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.component.AppTopBar
import com.workernotfound.app.core.designsystem.component.ComingSoonScreen

/**
 * 리뷰 작성 destination from 지난 공고 상세 (UI spec 2-6). The review-writing screen
 * is not defined in the UI spec, so this stays a placeholder
 * (decision: Demo Scope and Mock-First Strategy).
 */
@Composable
fun OwnerReviewPlaceholderScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Surface)
            .statusBarsPadding(),
    ) {
        AppTopBar(title = "리뷰 작성", onBack = onBack)
        ComingSoonScreen(title = "리뷰 작성")
    }
}
