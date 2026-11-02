package com.workernotfound.app.feature.location

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.location.*
import com.workernotfound.app.core.maps.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class LocationPickerUiState(val busy: Boolean = false, val candidates: List<ResolvedAddress> = emptyList(), val error: Boolean = false)
@HiltViewModel
class LocationPickerViewModel @Inject constructor(private val addresses: AddressResolver,
    private val positions: DeviceLocationProvider) : ViewModel() {
    private val state = MutableStateFlow(LocationPickerUiState())
    val uiState = state.asStateFlow()
    private val selected = MutableSharedFlow<ResolvedAddress>()
    val picks = selected.asSharedFlow()
    private var lookup: Job? = null
    fun find(query: String) = lookup { addresses.find(query) }
    fun fromMap(latitude: Double, longitude: Double) = lookup { addresses.reverse(latitude, longitude) }
    fun current() = lookup { val point = positions.current(); addresses.reverse(point.latitude, point.longitude) }
    fun denied() { state.value = LocationPickerUiState(error = true) }
    fun select(value: ResolvedAddress) {
        if (value in state.value.candidates) viewModelScope.launch { selected.emit(value); state.value = LocationPickerUiState() }
    }
    private fun lookup(block: suspend () -> List<ResolvedAddress>) {
        lookup?.cancel()
        lookup = viewModelScope.launch {
            state.value = LocationPickerUiState(busy = true)
            try { val candidates = block(); state.value = LocationPickerUiState(candidates = candidates, error = candidates.isEmpty()) }
            catch (_: TimeoutCancellationException) { state.value = LocationPickerUiState(error = true) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { state.value = LocationPickerUiState(error = true) }
        }
    }
}
@Composable
fun LocationPicker(address: String, latitude: String, longitude: String, enabled: Boolean,
    onAddress: (String) -> Unit, onPick: (ResolvedAddress) -> Unit, modifier: Modifier = Modifier,
    viewModel: LocationPickerViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pick by rememberUpdatedState(onPick)
    LaunchedEffect(viewModel) { viewModel.picks.collect { pick(it) } }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true) viewModel.current() else viewModel.denied()
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
        OutlinedTextField(address, onAddress, label = { Text(stringResource(R.string.map_address)) }, enabled = enabled && !state.busy,
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Row {
            TextButton({ viewModel.find(address) }, enabled = enabled && !state.busy && address.isNotBlank()) { Text(stringResource(R.string.map_search_address)) }
            TextButton({ permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }, enabled = enabled && !state.busy) { Text(stringResource(R.string.map_use_current)) }
        }
        if (state.busy) CircularProgressIndicator()
        if (state.error) Text(stringResource(R.string.map_address_failed), color = AppColors.Danger)
        state.candidates.forEach { candidate -> TextButton({ viewModel.select(candidate) }, enabled = enabled && !state.busy) { Text(candidate.address) } }
        val lat = latitude.toDoubleOrNull(); val lng = longitude.toDoubleOrNull()
        if (lat != null && lng != null) Text(stringResource(R.string.map_location_selected), color = AppColors.Success)
        AppMap(if (lat != null && lng != null) listOf(MapPoint("selected",lat,lng,address)) else emptyList(),
            modifier = Modifier.fillMaxWidth().height(AppDimens.locationMapHeight),
            onMapPoint = if (enabled && !state.busy) viewModel::fromMap else null)
    }
}
