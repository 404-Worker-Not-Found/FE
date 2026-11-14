package com.workernotfound.app.feature.worker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.BuildConfig
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppRadius
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppTopBar
import com.workernotfound.app.core.designsystem.component.SectionLabel
import com.workernotfound.app.feature.location.AddressMap
import com.workernotfound.app.feature.worker.domain.model.WorkerJobDetail
import com.workernotfound.app.feature.worker.viewmodel.WorkerJobDetailViewModel

/** The exact job referenced by the conversation; read-only for either participant role. */
@Composable
fun ChatPostingScreen(onBack: () -> Unit, modifier: Modifier = Modifier,
    viewModel: WorkerJobDetailViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier.fillMaxSize().background(AppTheme.role.screenBackground).statusBarsPadding()) {
        AppTopBar(stringResource(R.string.chat_posting_title), onBack = onBack)
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppTheme.role.accent)
            }
            state.errorMessage != null -> Column(
                Modifier.fillMaxSize().padding(AppDimens.screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(requireNotNull(state.errorMessage), color = AppColors.Danger)
                com.workernotfound.app.core.designsystem.component.AppSecondaryButton(stringResource(R.string.chat_retry), viewModel::load)
            }
            else -> state.detail?.let { job ->
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AppDimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
                ) {
                    StoreCard(job)
                    ScheduleCard(job)
                    WageCard(job)
                    if (job.description.isNotBlank()) DescriptionCard(job.description)
                }
            }
        }
    }
}

@Composable
private fun StoreCard(job: WorkerJobDetail) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = job.category,
            color = AppTheme.role.accentPressed,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(AppTheme.role.accentTint, RoundedCornerShape(AppRadius.pill))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
        Spacer(Modifier.height(10.dp))
        Text(text = job.storeName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Place, contentDescription = null, tint = AppColors.TextSub, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = job.address, color = AppColors.TextSub, fontSize = 13.sp)
        }
        // 지도 키가 없을 때 빈 지도 칸을 남기지 않는다.
        if (BuildConfig.NAVER_MAP_CLIENT_ID.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            AddressMap(job.address, Modifier.fillMaxWidth().height(AppDimens.locationMapHeight))
        }
    }
}

@Composable
private fun ScheduleCard(job: WorkerJobDetail) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(text = "근무 일정")
        Spacer(Modifier.height(10.dp))
        InfoLine("근무 날짜", job.workDate)
        Spacer(Modifier.height(8.dp))
        InfoLine("근무 시간", job.workTimeRange)
        Spacer(Modifier.height(8.dp))
        InfoLine("총 근무시간", job.totalWorkHours)
    }
}

@Composable
private fun WageCard(job: WorkerJobDetail) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(text = "시급 정보")
        Spacer(Modifier.height(10.dp))
        InfoLine(stringResource(R.string.worker_detail_base_wage), "${"%,d".format(job.baseWage)}원")
        if (job.extraWage > 0) {
            Spacer(Modifier.height(8.dp))
            InfoLine(stringResource(R.string.worker_detail_extra_wage), "+${"%,d".format(job.extraWage)}원")
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = AppColors.Border)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.worker_detail_final_wage),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextMain,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.worker_detail_wage_unit, "%,d".format(job.finalWage)),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.role.accentPressed,
            )
        }
    }
}

@Composable
private fun DescriptionCard(description: String) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(text = "업무 내용")
        Spacer(Modifier.height(10.dp))
        Text(text = description, color = AppColors.TextMain, fontSize = 14.sp, lineHeight = 21.sp)
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = AppColors.TextSub, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(text = value, color = AppColors.TextMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
