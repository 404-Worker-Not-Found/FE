package com.workernotfound.app.feature.job.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.core.designsystem.component.AppStepper
import com.workernotfound.app.core.designsystem.component.AppTopBar
import com.workernotfound.app.core.designsystem.component.ChoiceChipRow
import com.workernotfound.app.core.designsystem.component.LabeledField
import com.workernotfound.app.core.designsystem.component.MapPlaceholder
import com.workernotfound.app.feature.job.domain.WageBreakdown
import com.workernotfound.app.feature.job.domain.model.JobCategory
import com.workernotfound.app.feature.job.domain.model.MIN_HOURLY_WAGE
import com.workernotfound.app.feature.job.domain.model.RecruitRadius
import com.workernotfound.app.feature.job.domain.model.WorkDay
import com.workernotfound.app.feature.job.ui.component.TimePickerField
import com.workernotfound.app.feature.job.viewmodel.DESCRIPTION_MAX_LENGTH
import com.workernotfound.app.feature.job.viewmodel.JobPostingUiState
import com.workernotfound.app.feature.job.viewmodel.JobPostingViewModel

@Composable
fun JobPostingScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: JobPostingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    JobPostingContent(
        uiState = uiState,
        onBack = onBack,
        onWorkDay = viewModel::onWorkDayChange,
        onStartTime = viewModel::onStartTimeChange,
        onEndTime = viewModel::onEndTimeChange,
        onDescription = viewModel::onDescriptionChange,
        onBaseWage = viewModel::onBaseWageChange,
        onRecruitCount = viewModel::onRecruitCountChange,
        onRadius = viewModel::onRadiusChange,
        onCategory = viewModel::onCategoryChange,
        onSubmit = viewModel::submit,
    )

    if (uiState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClick = onSuccess) { Text("홈으로", color = AppTheme.role.accent, fontWeight = FontWeight.Bold) }
            },
            title = { Text("등록 완료") },
            text = { Text("긴급 공고가 등록되었습니다. 주변 알바생에게 노출됩니다.") },
        )
    }

    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            confirmButton = {
                TextButton(onClick = viewModel::dismissError) { Text("확인", color = AppTheme.role.accent) }
            },
            title = { Text("등록 실패") },
            text = { Text(message) },
        )
    }
}

@Composable
private fun JobPostingContent(
    uiState: JobPostingUiState,
    onBack: () -> Unit,
    onWorkDay: (WorkDay) -> Unit,
    onStartTime: (String) -> Unit,
    onEndTime: (String) -> Unit,
    onDescription: (String) -> Unit,
    onBaseWage: (String) -> Unit,
    onRecruitCount: (Int) -> Unit,
    onRadius: (RecruitRadius) -> Unit,
    onCategory: (JobCategory) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(title = "긴급 공고 등록", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppDimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // 근무 날짜
            LabeledField(label = "근무 날짜", required = true) {
                ChoiceChipRow(
                    options = WorkDay.entries.map { it.label },
                    selectedIndex = WorkDay.entries.indexOf(uiState.workDay),
                    onSelect = { onWorkDay(WorkDay.entries[it]) },
                )
            }

            // 근무 시간
            LabeledField(label = "근무 시간", required = true) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TimePickerField(
                        value = uiState.startTime,
                        onPick = onStartTime,
                        placeholder = "시작",
                        modifier = Modifier.weight(1f),
                    )
                    TimePickerField(
                        value = uiState.endTime,
                        onPick = onEndTime,
                        placeholder = "종료",
                        modifier = Modifier.weight(1f),
                    )
                }
                if (!uiState.isTimeRangeValid) {
                    Spacer(Modifier.height(6.dp))
                    Text("종료 시간은 시작 시간보다 늦어야 합니다.", color = AppColors.Danger, fontSize = 12.sp)
                }
            }

            // 업무 내용
            LabeledField(label = "업무 내용", required = true) {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = onDescription,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("어떤 업무인지 입력해 주세요 (최대 ${DESCRIPTION_MAX_LENGTH}자)", color = AppColors.TextPlaceholder) },
                    supportingText = {
                        Text(
                            text = "${uiState.description.length}/$DESCRIPTION_MAX_LENGTH",
                            color = AppColors.TextSub,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    colors = fieldColors(),
                )
            }

            // 기본 시급
            LabeledField(label = "기본 시급", required = true) {
                OutlinedTextField(
                    value = uiState.baseWageInput,
                    onValueChange = onBaseWage,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.isWageBelowMinimum,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("숫자 입력", color = AppColors.TextPlaceholder) },
                    suffix = { Text("원") },
                    supportingText = {
                        Text(
                            text = if (uiState.isWageBelowMinimum) {
                                "최저시급 ${"%,d".format(MIN_HOURLY_WAGE)}원 이상 입력해 주세요."
                            } else {
                                "최저시급 ${"%,d".format(MIN_HOURLY_WAGE)}원 이상"
                            },
                            color = if (uiState.isWageBelowMinimum) AppColors.Danger else AppColors.TextSub,
                        )
                    },
                    colors = fieldColors(),
                )
            }

            // 자동 시급 가산
            WageBreakdownCard(wage = uiState.wage)

            // 모집 인원
            LabeledField(label = "모집 인원", required = true) {
                AppStepper(
                    value = uiState.recruitCount,
                    onValueChange = onRecruitCount,
                    min = 1,
                    max = 10,
                    suffix = "명",
                )
            }

            // 모집 반경
            LabeledField(label = "모집 반경", required = true) {
                ChoiceChipRow(
                    options = RecruitRadius.entries.map { it.label },
                    selectedIndex = RecruitRadius.entries.indexOf(uiState.radius),
                    onSelect = { onRadius(RecruitRadius.entries[it]) },
                )
            }

            // 위치 정보
            LabeledField(label = "위치 정보", required = true) {
                Text(text = uiState.address, color = AppColors.TextMain, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                MapPlaceholder(caption = "현재 위치 · 지도 선택 (Naver 연동 예정)")
            }

            // 업종 카테고리
            LabeledField(label = "업종 카테고리", required = true) {
                ChoiceChipRow(
                    options = JobCategory.entries.map { it.label },
                    selectedIndex = JobCategory.entries.indexOf(uiState.category),
                    onSelect = { onCategory(JobCategory.entries[it]) },
                )
            }

            Spacer(Modifier.height(4.dp))
            SubmitButton(uiState = uiState, onSubmit = onSubmit)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WageBreakdownCard(wage: WageBreakdown) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text("자동 시급 가산", fontWeight = FontWeight.Bold, color = AppColors.TextMain, fontSize = 14.sp)
        Spacer(Modifier.height(10.dp))
        WageRow("기본 시급", wage.base)
        WageRow("긴급 가산", wage.urgencyBonus)
        WageRow("야간 가산", wage.nightBonus)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("최종 시급", fontWeight = FontWeight.Bold, color = AppColors.TextMain, fontSize = 14.sp)
            Text(
                text = "${"%,d".format(wage.total)}원",
                fontWeight = FontWeight.Bold,
                color = AppTheme.role.accent,
                fontSize = 18.sp,
            )
        }
    }
}

@Composable
private fun WageRow(label: String, amount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = AppColors.TextSub, fontSize = 13.sp)
        Text(text = "${if (amount > 0) "+" else ""}${"%,d".format(amount)}원", color = AppColors.TextMain, fontSize = 13.sp)
    }
}

@Composable
private fun SubmitButton(uiState: JobPostingUiState, onSubmit: () -> Unit) {
    if (uiState.isSubmitting) {
        Row(modifier = Modifier.fillMaxWidth().height(52.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(color = AppTheme.role.accent)
        }
    } else {
        AppPrimaryButton(text = "긴급 공고 등록", onClick = onSubmit, enabled = uiState.canSubmit)
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppTheme.role.accent,
    unfocusedBorderColor = AppColors.Border,
    cursorColor = AppTheme.role.accent,
    focusedContainerColor = AppColors.CardSurface,
    unfocusedContainerColor = AppColors.CardSurface,
)
