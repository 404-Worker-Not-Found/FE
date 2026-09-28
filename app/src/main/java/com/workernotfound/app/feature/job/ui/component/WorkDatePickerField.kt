package com.workernotfound.app.feature.job.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppRadius
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.feature.job.domain.model.WorkDay
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val DATE_LABEL: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

/**
 * 근무 날짜 field (UI spec 2-2: 달력 팝업에서 날짜 선택, 당일 또는 익일 제한).
 * Opens a calendar dialog in which only today and tomorrow are selectable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkDatePickerField(
    value: WorkDay?,
    onPick: (WorkDay) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "날짜 선택",
) {
    var showDialog by remember { mutableStateOf(false) }
    val today = remember { LocalDate.now() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.CardSurface, RoundedCornerShape(AppRadius.inner))
            .border(BorderStroke(1.dp, AppColors.Border), RoundedCornerShape(AppRadius.inner))
            .clickable { showDialog = true }
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = value?.let { "${today.plusDays(it.offsetDays()).format(DATE_LABEL)} (${it.label})" } ?: placeholder,
            color = if (value == null) AppColors.TextPlaceholder else AppColors.TextMain,
            fontSize = 14.sp,
        )
    }

    if (showDialog) {
        val selectable: Map<Long, WorkDay> = remember(today) {
            WorkDay.entries.associateBy { today.plusDays(it.offsetDays()).toUtcMillis() }
        }
        val years = today.year..today.plusDays(1).year
        val state = rememberDatePickerState(
            initialSelectedDateMillis = value?.let { today.plusDays(it.offsetDays()).toUtcMillis() },
            yearRange = years,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis in selectable
                override fun isSelectableYear(year: Int): Boolean = year in years
            },
        )
        val accent = AppTheme.role.accent
        val pickerColors = DatePickerDefaults.colors(
            containerColor = AppColors.CardSurface,
            selectedDayContainerColor = accent,
            todayDateBorderColor = accent,
            todayContentColor = accent,
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                val picked = state.selectedDateMillis?.let(selectable::get)
                TextButton(
                    onClick = {
                        picked?.let(onPick)
                        showDialog = false
                    },
                    enabled = picked != null,
                ) {
                    Text("선택", color = if (picked != null) accent else AppColors.TextPlaceholder)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("취소", color = AppColors.TextSub)
                }
            },
            colors = pickerColors,
        ) {
            DatePicker(state = state, colors = pickerColors, showModeToggle = false)
        }
    }
}

private fun WorkDay.offsetDays(): Long = when (this) {
    WorkDay.TODAY -> 0L
    WorkDay.TOMORROW -> 1L
}

/** Material3 DatePicker works with UTC-midnight millis. */
private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
