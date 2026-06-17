package com.workernotfound.app.feature.worker.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.feature.worker.domain.model.JobFilter
import com.workernotfound.app.feature.worker.domain.model.TimeSlot

private val DISTANCE_OPTIONS = listOf(1, 3, 5, 10)
private val WAGE_OPTIONS = listOf(0, 11000, 12000, 13000)
private val CATEGORY_OPTIONS = listOf("음식점", "카페", "편의점")

/**
 * Multi-filter bottom sheet for job search (UI spec 3-2: 거리 / 시급 / 시간대 / 업종).
 * Edits a local copy and applies it on confirm.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobFilterSheet(
    current: JobFilter,
    onApply: (JobFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember { mutableStateOf(current) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = AppColors.CardSurface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.screenPadding)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "필터", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)

            FilterGroup(title = "긴급") {
                SelectChip(
                    label = "⚡ 긴급 공고만",
                    selected = draft.urgentOnly,
                    onClick = { draft = draft.copy(urgentOnly = !draft.urgentOnly) },
                )
            }

            FilterGroup(title = "거리") {
                DISTANCE_OPTIONS.forEach { km ->
                    SelectChip(
                        label = "${km}km",
                        selected = draft.maxDistanceKm == km,
                        onClick = { draft = draft.copy(maxDistanceKm = km) },
                    )
                }
            }

            FilterGroup(title = "시급") {
                WAGE_OPTIONS.forEach { wage ->
                    SelectChip(
                        label = if (wage == 0) "전체" else "${"%,d".format(wage)}원+",
                        selected = draft.minWage == wage,
                        onClick = { draft = draft.copy(minWage = wage) },
                    )
                }
            }

            FilterGroup(title = "시간대") {
                TimeSlot.entries.forEach { slot ->
                    SelectChip(
                        label = slot.label,
                        selected = draft.timeSlot == slot,
                        onClick = { draft = draft.copy(timeSlot = slot) },
                    )
                }
            }

            FilterGroup(title = "업종") {
                CATEGORY_OPTIONS.forEach { category ->
                    SelectChip(
                        label = category,
                        selected = category in draft.categories,
                        onClick = {
                            draft = draft.copy(
                                categories = draft.categories.toggle(category),
                            )
                        },
                    )
                }
            }

            AppPrimaryButton(text = "적용하기", onClick = { onApply(draft) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSub)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppTheme.role.accent,
            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
        ),
    )
}

private fun Set<String>.toggle(value: String): Set<String> =
    if (value in this) this - value else this + value
