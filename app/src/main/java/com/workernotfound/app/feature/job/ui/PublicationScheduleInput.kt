package com.workernotfound.app.feature.job.ui

import androidx.compose.foundation.layout.*
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.feature.job.viewmodel.PublicationField
import java.time.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PublicationScheduleInput(field: PublicationField, value: String, label: String,
    enabled: Boolean, startTime: String? = null, workDate: String? = null, onValue: (String) -> Unit) {
    if (field == PublicationField.START || field == PublicationField.END) {
        Column {
            Text(label, style = MaterialTheme.typography.labelLarge)
            com.workernotfound.app.feature.job.ui.component.TimePickerField(value, onValue,
                placeholder = stringResource(R.string.time_select_placeholder), startTime = if (field == PublicationField.END) startTime else null, enabled = enabled)
        }
        return
    }
    if (field == PublicationField.DEADLINE) {
        Column {
            Text(label, style = MaterialTheme.typography.labelLarge)
            com.workernotfound.app.feature.job.ui.component.DeadlinePickerField(value.ifBlank { null },
                workDate?.ifBlank { null }, startTime?.ifBlank { null }, onValue, enabled = enabled)
        }
        return
    }
    var dateOpen by rememberSaveable { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        OutlinedButton(
            onClick = { dateOpen = true },
            enabled = enabled, modifier = Modifier.fillMaxWidth(),
        ) {
            val display = runCatching {
                when (field) {
                    PublicationField.DATE -> LocalDate.parse(value).format(DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN))
                    else -> value
                }
            }.getOrDefault(value)
            Text(display.ifBlank { stringResource(R.string.publication_choose_schedule, label) })
        }
    }
    if (dateOpen) {
        val initial = runCatching { LocalDate.parse(value.substringBefore('T')).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }.getOrNull()
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val available = remember(today) { object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate() in today..today.plusDays(1)
            override fun isSelectableYear(year: Int) = year in today.year..today.plusDays(1).year
        } }
        val picker = remember(initial, available) { DatePickerState(locale = Locale.KOREAN, initialSelectedDateMillis = initial, selectableDates = available) }
        DatePickerDialog(onDismissRequest = { dateOpen = false }, confirmButton = {
            TextButton({
                val date = picker.selectedDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString() }
                if (date != null) {
                    dateOpen = false
                    onValue(date)
                }
            }, enabled = picker.selectedDateMillis != null) { Text(stringResource(R.string.publication_picker_confirm)) }
        }, dismissButton = { TextButton({ dateOpen = false }) { Text(stringResource(R.string.publication_picker_cancel)) } }) { DatePicker(picker, showModeToggle = false, title = { Text(label, modifier = Modifier.padding(AppDimens.screenPadding)) }) }
    }
}
