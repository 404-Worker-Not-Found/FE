package com.workernotfound.app.feature.worker.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.BuildConfig
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.location.AddressResolver
import com.workernotfound.app.core.location.ResolvedAddress
import com.workernotfound.app.core.maps.*
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import com.workernotfound.app.feature.worker.domain.repository.WorkerJobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

data class JobMapUiState(val loading: Boolean = false, val points: List<MapPoint> = emptyList(), val missing: Boolean = false)
@HiltViewModel
class JobMapViewModel @Inject constructor(private val jobs: WorkerJobRepository,
    private val addresses: AddressResolver) : ViewModel() {
    private val state = MutableStateFlow(JobMapUiState())
    val uiState = state.asStateFlow()
    private val cache = mutableMapOf<String, ResolvedAddress>()
    private var lookup: Job? = null
    fun load(postings: List<WorkerJobPosting>) {
        lookup?.cancel()
        lookup = viewModelScope.launch {
            state.value = JobMapUiState(loading = true)
            if (!addresses.available()) { state.value = JobMapUiState(missing = postings.isNotEmpty()); return@launch }
            val limit = Semaphore(4)
            val points = coroutineScope {
                postings.distinctBy { it.id }.map { posting -> async {
                    limit.withPermit {
                        try {
                            val address = jobs.getJobDetail(posting.id).address
                            val position = cache[address] ?: addresses.find(address).firstOrNull()?.also { cache[address] = it }
                            position?.let { MapPoint(posting.id, it.latitude, it.longitude, posting.storeName) }
                        } catch (_: TimeoutCancellationException) { null }
                        catch (e: CancellationException) { throw e }
                        catch (_: Exception) { null }
                    }
                } }.awaitAll().filterNotNull()
            }
            state.value = JobMapUiState(points = points, missing = points.size < postings.distinctBy { it.id }.size)
        }
    }
}

@Composable
fun LiveJobMap(jobs: List<WorkerJobPosting>, selectedId: String?, onSelect: (String) -> Unit,
    latitude: Double?, longitude: Double?, onJobClick: (String) -> Unit, onApply: (String) -> Unit,
    modifier: Modifier = Modifier) {
    if (BuildConfig.NAVER_MAP_CLIENT_ID.isBlank()) { AppMap(emptyList(), modifier.fillMaxSize()); return }
    val viewModel: JobMapViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(jobs) { viewModel.load(jobs) }
    val currentLabel = stringResource(R.string.map_my_location)
    val points = state.points + if (latitude != null && longitude != null)
        listOf(MapPoint("current-location", latitude, longitude, currentLabel)) else emptyList()
    Column(modifier.fillMaxSize()) {
        if (state.loading) Text(stringResource(R.string.map_locations_loading), color = AppColors.TextSub)
        if (state.missing) Text(stringResource(R.string.map_locations_missing), color = AppColors.TextSub)
        AppMap(points, Modifier.fillMaxWidth().weight(1f), onMarker = { id -> if (id != "current-location") onSelect(id) })
        jobs.firstOrNull { it.id == selectedId }?.let { selected ->
            WorkerRecommendedJobCard(selected, { onJobClick(selected.id) }, { onApply(selected.id) },
                modifier = Modifier.fillMaxWidth().padding(AppDimens.screenPadding))
        }
    }
}
