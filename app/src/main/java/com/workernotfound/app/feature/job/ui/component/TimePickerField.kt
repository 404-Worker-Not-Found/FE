package com.workernotfound.app.feature.job.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

/** 30-minute time slots from 00:00 to 23:30 (UI spec 2-2: 30분 단위). */
private val TIME_SLOTS: List<String> = buildList {
    for (hour in 0..23) {
        add("%02d:00".format(hour))
        add("%02d:30".format(hour))
    }
}

@Composable
fun TimePickerField(
    value: String?,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "선택",
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.CardSurface, RoundedCornerShape(AppRadius.inner))
            .border(BorderStroke(1.dp, AppColors.Border), RoundedCornerShape(AppRadius.inner))
            .clickable { showDialog = true }
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = value ?: placeholder,
            color = if (value == null) AppColors.TextPlaceholder else AppColors.TextMain,
            fontSize = 14.sp,
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("닫기", color = AppTheme.role.accent)
                }
            },
            title = { Text("시간 선택", fontSize = 16.sp) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                    items(TIME_SLOTS) { slot ->
                        Text(
                            text = slot,
                            color = if (slot == value) AppTheme.role.accent else AppColors.TextMain,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPick(slot)
                                    showDialog = false
                                }
                                .padding(vertical = 12.dp),
                        )
                    }
                }
            },
        )
    }
}
