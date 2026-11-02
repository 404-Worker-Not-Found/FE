package com.workernotfound.app.feature.worker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.feature.worker.viewmodel.AttendanceProblem
import com.workernotfound.app.feature.worker.viewmodel.WorkerAttendanceUiState
import com.workernotfound.app.feature.worker.viewmodel.WorkerAttendanceViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
internal fun WorkerAttendancePanel(modifier: Modifier = Modifier, viewModel: WorkerAttendanceViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingSubmit by rememberSaveable { mutableStateOf(false) }
    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        val id = pendingId
        pendingId = null
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true && id != null) {
            if (pendingSubmit) viewModel.checkIn(id) else viewModel.locate(id)
        } else viewModel.permissionDenied()
    }
    fun locate(id: String, submit: Boolean) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (submit) viewModel.checkIn(id) else viewModel.locate(id)
        } else {
            pendingId = id
            pendingSubmit = submit
            permissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
    WorkerAttendanceContent(state, viewModel::load, ::locate, onSettings = { location ->
        val intent = if (location) Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        context.startActivity(intent)
    }, modifier)
}

@Composable
private fun WorkerAttendanceContent(
    state: WorkerAttendanceUiState,
    onRefresh: () -> Unit,
    onPosition: (String, Boolean) -> Unit,
    onSettings: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(AppDimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap),
    ) {
        item {
            Text(stringResource(R.string.attendance_trust_pending), color = AppColors.TextSub)
            TextButton(onClick = onRefresh, enabled = !state.isLoading && state.busyWorkId == null) {
                Text(stringResource(R.string.attendance_refresh))
            }
            if (state.isLoading) CircularProgressIndicator()
            state.errorMessage?.let { Text(it, color = AppColors.Danger) }
            state.problem?.let { problem ->
                Text(stringResource(attendanceProblemResource(problem)), color = AppColors.Danger)
                if (problem == AttendanceProblem.PERMISSION || problem == AttendanceProblem.DISABLED) {
                    TextButton(onClick = { onSettings(problem == AttendanceProblem.DISABLED) }) {
                        Text(stringResource(if (problem == AttendanceProblem.DISABLED) R.string.attendance_open_location else R.string.attendance_open_permissions))
                    }
                }
            }
            if (!state.isLoading && state.errorMessage == null && state.works.isEmpty()) Text(stringResource(R.string.attendance_no_works), color = AppColors.TextSub)
        }
        items(state.works, key = { it.id }) { work ->
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
                    Text(work.storeName)
                    Text("${work.workDate} · ${work.timeRange}", color = AppColors.TextSub)
                    work.address?.let { Text(it, color = AppColors.TextSub) }
                    val attendance = work.attendance
                    when {
                        attendance == null -> Text(stringResource(R.string.attendance_missing_target))
                        attendance.checkedIn -> Text(stringResource(R.string.attendance_complete), color = AppColors.Success)
                        else -> {
                            val format = DateTimeFormatter.ofPattern("MM.dd HH:mm").withZone(ZoneId.of(attendance.timeZone))
                            Text(stringResource(R.string.attendance_window,
                                format.format(Instant.ofEpochMilli(attendance.earliestCheckInMillis)),
                                format.format(Instant.ofEpochMilli(attendance.latestCheckInMillis))), color = AppColors.TextSub)
                            val inTime = attendance.allowsTime(state.nowMillis)
                            val ready = state.preparedWorkId == work.id
                            if (ready && state.distanceMeters != null) Text(stringResource(R.string.attendance_distance,
                                state.distanceMeters.roundToInt(), attendance.radiusMeters.toBigDecimal().stripTrailingZeros().toPlainString()))
                            if (state.busyWorkId == work.id) Text(stringResource(R.string.attendance_locating))
                            if (attendance.latitude == null || attendance.longitude == null) Text(stringResource(R.string.attendance_missing_target), color = AppColors.TextSub)
                            if (!inTime) Text(stringResource(R.string.attendance_time), color = AppColors.TextSub)
                            TextButton(onClick = { onPosition(work.id, false) }, enabled = !state.isLoading && inTime && attendance.latitude != null && attendance.longitude != null && state.busyWorkId == null) {
                                Text(stringResource(R.string.attendance_prepare))
                            }
                            AppPrimaryButton(text = stringResource(R.string.attendance_submit), onClick = { onPosition(work.id, true) },
                                enabled = !state.isLoading && ready && inTime && state.problem == null && state.busyWorkId == null)
                        }
                    }
                }
            }
        }
    }
}

internal fun attendanceProblemResource(problem: AttendanceProblem): Int = when (problem) {
    AttendanceProblem.PERMISSION -> R.string.attendance_permission
    AttendanceProblem.DISABLED -> R.string.attendance_disabled
    AttendanceProblem.UNAVAILABLE -> R.string.attendance_unavailable
    AttendanceProblem.TIMEOUT -> R.string.attendance_timeout
    AttendanceProblem.OUTSIDE -> R.string.attendance_outside
    AttendanceProblem.TIME -> R.string.attendance_time
    AttendanceProblem.MISSING_TARGET -> R.string.attendance_missing_target
}
