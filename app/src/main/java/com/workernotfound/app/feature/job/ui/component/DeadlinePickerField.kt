package com.workernotfound.app.feature.job.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.workernotfound.app.R
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Application deadline dropdown anchored to its field. Only 30-minute slots after now and before the
 * work start are offered, so the user cannot pick a past time or one after the shift begins.
 */
@Composable
fun DeadlinePickerField(value: String?, workDate: String?, startTime: String?, onPick: (String) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true) {
    var open by remember { mutableStateOf(false) }
    val workStart = runCatching { LocalDate.parse(workDate).atTime(LocalTime.parse(startTime)) }.getOrNull()
    val now = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
    val slots = remember(workStart, open) { deadlineSlots(now, workStart) }
    val selected = value?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
    val today = now.toLocalDate()
    val timeFormat = remember { DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN) }
    val density = LocalDensity.current
    val scroll = rememberScrollState()
    val selectedIndex = slots.indexOf(selected).coerceAtLeast(0)
    LaunchedEffect(open) { if (open) scroll.scrollTo(with(density) { (selectedIndex * 48).dp.roundToPx() }) }
    Box(modifier) {
        OutlinedButton({ open = true }, enabled = enabled && slots.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
            Text(when {
                selected != null -> dayTime(selected, today, timeFormat)
                workStart == null -> stringResource(R.string.deadline_need_schedule)
                slots.isEmpty() -> stringResource(R.string.deadline_none_available)
                else -> stringResource(R.string.time_select_placeholder)
            })
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false },
            modifier = Modifier.width(260.dp).heightIn(max = 280.dp), scrollState = scroll) {
            slots.forEach { slot ->
                val before = timeDuration(Duration.between(slot, requireNotNull(workStart)).toMinutes().toInt())
                DropdownMenuItem(text = { Text(stringResource(R.string.deadline_option, dayTime(slot, today, timeFormat), before)) },
                    onClick = { onPick(slot.toString()); open = false })
            }
        }
    }
}

@Composable
private fun dayTime(at: LocalDateTime, today: LocalDate, format: DateTimeFormatter): String = stringResource(
    if (at.toLocalDate() == today) R.string.deadline_today else R.string.deadline_tomorrow, at.format(format))

/** 30-minute slots strictly after [now] and strictly before [workStart], nearest first. */
internal fun deadlineSlots(now: LocalDateTime, workStart: LocalDateTime?): List<LocalDateTime> {
    if (workStart == null) return emptyList()
    val base = now.withSecond(0).withNano(0)
    var slot = if (base.minute < 30) base.withMinute(30) else base.withMinute(0).plusHours(1)
    return buildList { while (slot < workStart) { add(slot); slot = slot.plusMinutes(30) } }
}

@Composable
internal fun timeDuration(minutes: Int): String = when {
    minutes < 60 -> stringResource(R.string.time_duration_only_minutes, minutes)
    minutes % 60 == 0 -> stringResource(R.string.time_duration_hours, minutes / 60)
    else -> stringResource(R.string.time_duration_minutes, minutes / 60, minutes % 60)
}
