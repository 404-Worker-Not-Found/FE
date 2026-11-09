package com.workernotfound.app.feature.location

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.BuildConfig
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.maps.AppMap
import com.workernotfound.app.core.maps.MapPoint

/** Address-derived location is approximate; never substitutes for a check-in GPS fix. */
@Composable
fun AddressMap(address: String, modifier: Modifier = Modifier) {
    if (BuildConfig.NAVER_MAP_CLIENT_ID.isBlank()) { AppMap(emptyList(), modifier); return }
    val viewModel: LocationPickerViewModel = hiltViewModel(key = "address-map-$address")
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(address) { viewModel.find(address) }
    Column {
        if (state.error) Text(stringResource(R.string.map_locations_missing), color = AppColors.TextSub)
        AppMap(state.candidates.firstOrNull()?.let { listOf(MapPoint("address", it.latitude, it.longitude, address)) }.orEmpty(), modifier)
    }
}
