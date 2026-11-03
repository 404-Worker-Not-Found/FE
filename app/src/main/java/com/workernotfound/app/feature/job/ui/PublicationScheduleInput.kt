package com.workernotfound.app.feature.job.ui

import androidx.compose.foundation.layout.*
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
    enabled: Boolean, onValue: (String) -> Unit) {
    var dateOpen by rememberSaveable { mutableStateOf(false) }
    var timeOpen by rememberSaveable { mutableStateOf(false) }
    var selectedDate by rememberSaveable { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
        OutlinedTextField(value, onValue, label = { Text(label) }, enabled = enabled,
            singleLine = true, modifier = Modifier.fillMaxWidth())
        TextButton({ if (field == PublicationField.DATE || field == PublicationField.DEADLINE) dateOpen = true else timeOpen = true }, enabled = enabled) {
            Text(stringResource(R.string.publication_choose_schedule, label))
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
        val picker = rememberDatePickerState(initialSelectedDateMillis = initial, selectableDates = available)
        DatePickerDialog(onDismissRequest = { dateOpen = false }, confirmButton = {
            TextButton({
                val date = picker.selectedDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString() }
                if (date != null) {
                    dateOpen = false
                    if (field == PublicationField.DEADLINE) { selectedDate = date; timeOpen = true } else onValue(date)
                }
            }, enabled = picker.selectedDateMillis != null) { Text(stringResource(R.string.publication_picker_confirm)) }
        }, dismissButton = { TextButton({ dateOpen = false }) { Text(stringResource(R.string.publication_picker_cancel)) } }) { DatePicker(picker) }
    }
    if (timeOpen) {
        val initial = runCatching { LocalTime.parse(value.substringAfter('T')) }.getOrNull()
        val picker = rememberTimePickerState(initialHour = initial?.hour ?: 9, initialMinute = initial?.minute ?: 0, is24Hour = true)
        AlertDialog(onDismissRequest = { timeOpen = false }, title = { Text(label) }, text = { TimeInput(picker) },
            confirmButton = { TextButton({
                val time = LocalTime.of(picker.hour, picker.minute).toString()
                if (field == PublicationField.DEADLINE) selectedDate?.let { onValue("${it}T$time") } else onValue(time)
                timeOpen = false
            }) { Text(stringResource(R.string.publication_picker_confirm)) } },
            dismissButton = { TextButton({ timeOpen = false }) { Text(stringResource(R.string.publication_picker_cancel)) } })
    }
}
