package com.workernotfound.app.feature.job.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.workernotfound.app.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TimePickerField(value: String?, onPick: (String) -> Unit, modifier: Modifier = Modifier,
    placeholder: String = "선택", startTime: String? = null, enabled: Boolean = true,
    sameDayOnly: Boolean = false) {
    var open by remember { mutableStateOf(false) }
    val start = startTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
    val slots = remember(start, sameDayOnly) {
        // End options are measured from the start; overnight ends are inferred by the caller.
        val steps = when {
            start == null -> 0 until 48
            sameDayOnly -> 1..((23 * 60 + 30 - start.toSecondOfDay() / 60) / 30)
            else -> 1..48
        }
        steps.map { step -> (start ?: LocalTime.MIDNIGHT).plusMinutes(step * 30L) to step * 30 }
    }
    val format = remember { DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN) }
    val selected = value?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
    val selectedIndex = slots.indexOfFirst { it.first == selected }.takeIf { it >= 0 } ?: if (start == null) 18 else 0
    val density = androidx.compose.ui.platform.LocalDensity.current
    val scroll = rememberScrollState()
    LaunchedEffect(open) { if (open) scroll.scrollTo(with(density) { (selectedIndex * 48).dp.roundToPx() }) }
    Box(modifier) {
        OutlinedButton({ open = true }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Text(selected?.format(format) ?: placeholder)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false },
            modifier = Modifier.width(240.dp).heightIn(max = 280.dp), scrollState = scroll) {
            slots.forEach { (time, minutes) ->
                val duration = timeDuration(minutes)
                DropdownMenuItem(text = { Text(if (start == null) time.format(format)
                    else stringResource(R.string.time_end_option, time.format(format), duration)) },
                    onClick = { onPick(time.toString()); open = false })
            }
        }
    }
}
