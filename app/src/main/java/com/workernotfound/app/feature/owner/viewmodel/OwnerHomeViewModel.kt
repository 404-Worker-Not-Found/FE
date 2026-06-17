package com.workernotfound.app.feature.owner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.feature.owner.domain.model.OwnerHomeSummary
import com.workernotfound.app.feature.owner.domain.model.PostingStatus
import com.workernotfound.app.feature.owner.domain.repository.OwnerHomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerHomeViewModel @Inject constructor(
    private val repository: OwnerHomeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerHomeUiState())
    val uiState: StateFlow<OwnerHomeUiState> = _uiState.asStateFlow()

    init {
        loadPostings()
    }

    fun loadPostings() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getMyPostings() }
                .onSuccess { postings ->
                    val activeCount = postings.count { it.status == PostingStatus.RECRUITING }
                    val totalApplicants = postings.sumOf { it.applicantCount }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            postings = postings,
                            summary = OwnerHomeSummary(activeCount, totalApplicants),
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "공고를 불러오지 못했습니다.",
                        )
                    }
                }
        }
    }
}
