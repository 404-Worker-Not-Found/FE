package com.workernotfound.app.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppRole
import com.workernotfound.app.core.designsystem.WorkerNotFoundTheme
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton

/**
 * Demo entry point: choose the role to start from. Replaces the auth flow,
 * which is out of demo scope (decision: Demo Scope and Mock-First Strategy).
 */
@Composable
fun RoleSwitcherScreen(
    onSelectOwner: () -> Unit,
    onSelectWorker: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Surface)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "⚡",
            fontSize = 56.sp,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "긴급알바",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextMain,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "데모를 시작할 역할을 선택하세요",
            fontSize = 14.sp,
            color = AppColors.TextSub,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))

        WorkerNotFoundTheme(role = AppRole.OWNER) {
            AppPrimaryButton(text = "점주로 시작", onClick = onSelectOwner)
        }
        Spacer(Modifier.height(12.dp))
        WorkerNotFoundTheme(role = AppRole.WORKER) {
            AppPrimaryButton(text = "알바생으로 시작", onClick = onSelectWorker)
        }
    }
}
