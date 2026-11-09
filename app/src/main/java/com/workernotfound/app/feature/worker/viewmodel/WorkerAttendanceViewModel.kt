package com.workernotfound.app.feature.worker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.core.location.DeviceLocationException
import com.workernotfound.app.core.location.DeviceLocationProvider
import com.workernotfound.app.core.location.LocationFailure
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.worker.domain.model.WorkSchedule
import com.workernotfound.app.feature.worker.domain.repository.WorkerWorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AttendanceProblem { PERMISSION, DISABLED, UNAVAILABLE, TIMEOUT, OUTSIDE, TIME, MISSING_TARGET }
data class WorkerAttendanceUiState(
    val works: List<WorkSchedule> = emptyList(),
    val isLoading: Boolean = true,
    val busyWorkId: String? = null,
    val preparedWorkId: String? = null,
    val distanceMeters: Double? = null,
    val problem: AttendanceProblem? = null,
    val errorMessage: String? = null,
    val nowMillis: Long = System.currentTimeMillis(),
)

@HiltViewModel
class WorkerAttendanceViewModel @Inject constructor(
    private val repository: WorkerWorkRepository,
    private val locations: DeviceLocationProvider,
    private val backend: BackendAccess,
) : ViewModel() {
    private val state = MutableStateFlow(WorkerAttendanceUiState())
    val uiState = state.asStateFlow()
    init {
        load()
        viewModelScope.launch {
            while (true) { delay(1000); state.update { it.copy(nowMillis = System.currentTimeMillis()) } }
        }
    }

    fun load() {
        if (state.value.busyWorkId != null) return
        state.update { it.copy(isLoading = true, errorMessage = null, problem = null, preparedWorkId = null) }
        viewModelScope.launch {
            try {
                val works = repository.getWorks()
                state.update { it.copy(works = works, isLoading = false) }
            }
            catch (error: Exception) {
                if (error is CancellationException) throw error
                state.update { it.copy(isLoading = false, works = emptyList(), errorMessage = backend.message(error)) }
            }
        }
    }

    fun permissionDenied() = state.update { it.copy(problem = AttendanceProblem.PERMISSION, preparedWorkId = null) }
    fun locate(workId: String) = readPosition(workId, submit = false)
    fun checkIn(workId: String) = readPosition(workId, submit = true)

    private fun readPosition(workId: String, submit: Boolean) {
        if (state.value.isLoading || state.value.busyWorkId != null) return
        val work = state.value.works.firstOrNull { it.id == workId } ?: return
        val attendance = work.attendance ?: return
        if (attendance.checkedIn) return
        if (!attendance.allowsTime(System.currentTimeMillis())) {
            state.update { it.copy(problem = AttendanceProblem.TIME, preparedWorkId = null) }; return
        }
        if (attendance.latitude == null || attendance.longitude == null) {
            state.update { it.copy(problem = AttendanceProblem.MISSING_TARGET, preparedWorkId = null) }; return
        }
        state.update { it.copy(busyWorkId = workId, problem = null, errorMessage = null, preparedWorkId = null) }
        viewModelScope.launch {
            try {
                val point = locations.current()
                val distance = requireNotNull(attendance.distanceFrom(point.latitude, point.longitude))
                if (distance > attendance.radiusMeters) {
                    state.update { it.copy(distanceMeters = distance, problem = AttendanceProblem.OUTSIDE) }
                } else if (!attendance.allowsTime(System.currentTimeMillis())) {
                    state.update { it.copy(problem = AttendanceProblem.TIME) }
                } else if (submit) {
                    // Acquire a fresh fix for submission; the server makes the final decision.
                    val receipt = repository.checkIn(workId, point.latitude, point.longitude)
                    state.update { current -> current.copy(works = current.works.map {
                        if (it.id == receipt.workId) it.copy(attendance = it.attendance?.copy(checkedIn = true, status = receipt.status)) else it
                    }, distanceMeters = null) }
                } else state.update { it.copy(preparedWorkId = workId, distanceMeters = distance) }
            } catch (error: DeviceLocationException) {
                state.update { it.copy(problem = when (error.reason) {
                    LocationFailure.PERMISSION -> AttendanceProblem.PERMISSION
                    LocationFailure.DISABLED -> AttendanceProblem.DISABLED
                    LocationFailure.UNAVAILABLE -> AttendanceProblem.UNAVAILABLE
                    LocationFailure.TIMEOUT -> AttendanceProblem.TIMEOUT
                }) }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                state.update { it.copy(errorMessage = backend.message(error)) }
            } finally { state.update { it.copy(busyWorkId = null) } }
        }
    }
}
