package com.workernotfound.app.feature.job.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.job.domain.model.*
import com.workernotfound.app.feature.job.domain.repository.JobPublicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.*
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class PublicationField { TITLE, DESCRIPTION, DATE, START, END, BASE_WAGE, EXTRA_WAGE, COUNT, DEADLINE, RESUME_ID }
data class JobPublicationUiState(
    val fields: Map<PublicationField, String> = emptyMap(), val business: PostingBusiness? = null,
    val locationSelected: Boolean = true,
    val nextDay: Boolean = false, val urgency: String = "MEDIUM",
    val categories: List<PostingCategory> = emptyList(), val selectedCategory: PostingCategory? = null,
    val busy: Boolean = false, val error: String? = null, val jobId: Long? = null,
    val creationAttempted: Boolean = false, val payment: PublicationPayment? = null,
) {
    operator fun get(field: PublicationField) = fields[field].orEmpty()
    fun draft(): PublicationDraft? = try {
        business?.takeIf { locationSelected }?.let { PublicationDraft(it, requireNotNull(selectedCategory).id, this[PublicationField.TITLE],
            this[PublicationField.DESCRIPTION], LocalDate.parse(this[PublicationField.DATE]), LocalTime.parse(this[PublicationField.START]),
            LocalTime.parse(this[PublicationField.END]), nextDay, this[PublicationField.BASE_WAGE].toInt(),
            this[PublicationField.EXTRA_WAGE].takeIf { it.isNotBlank() }?.toInt(), this[PublicationField.COUNT].toInt(),
            urgency, LocalDateTime.parse(this[PublicationField.DEADLINE])) }
    } catch (_: IllegalArgumentException) { null } catch (_: java.time.format.DateTimeParseException) { null }
    val canCreate get() = !busy && !creationAttempted && selectedCategory != null && selectedCategory in categories &&
        draft()?.isValid(LocalDateTime.now(ZoneId.of("Asia/Seoul"))) == true
}

@HiltViewModel
class JobPublicationViewModel @Inject constructor(
    private val repository: JobPublicationRepository, private val backend: BackendAccess, private val saved: SavedStateHandle,
) : ViewModel() {
    private val state = MutableStateFlow(JobPublicationUiState(jobId = saved["publicationJobId"], creationAttempted = saved["publicationAttempted"] ?: false))
    val uiState = state.asStateFlow()
    init { if (state.value.jobId != null) refresh() else loadBusiness() }
    fun loadBusiness() {
        if (state.value.creationAttempted) return
        action {
        val business = repository.business()
        val categories = repository.categories()
        state.update { it.copy(business = business, categories = categories, locationSelected = true) }
        }
    }
    fun edit(field: PublicationField, value: String) {
        if (!state.value.busy && (!state.value.creationAttempted || field == PublicationField.RESUME_ID)) state.update { it.copy(fields = it.fields + (field to value), error = null) }
    }
    fun nextDay(value: Boolean) { if (!state.value.busy && !state.value.creationAttempted) state.update { it.copy(nextDay = value) } }
    fun urgency(value: String) { if (!state.value.busy && !state.value.creationAttempted) state.update { it.copy(urgency = value) } }
    fun category(value: PostingCategory) {
        if (!state.value.busy && !state.value.creationAttempted && value in state.value.categories) state.update { it.copy(selectedCategory = value) }
    }
    fun address(value: String) {
        if (state.value.busy || state.value.creationAttempted) return
        state.update { it.copy(business = it.business?.copy(address = value), locationSelected = false) }
    }
    fun location(value: com.workernotfound.app.core.location.ResolvedAddress) {
        if (state.value.busy || state.value.creationAttempted || !value.latitude.isFinite() || !value.longitude.isFinite()) return
        state.update { it.copy(business = it.business?.copy(address = value.address,
            latitude = java.math.BigDecimal.valueOf(value.latitude), longitude = java.math.BigDecimal.valueOf(value.longitude)), locationSelected = true) }
    }
    fun create() {
        val s = state.value
        if (!s.canCreate) return
        // The create endpoint has no idempotency key: never silently retry after an uncertain response.
        saved["publicationAttempted"] = true
        state.update { it.copy(creationAttempted = true) }
        action {
            val id = repository.create(requireNotNull(s.draft()))
            saved["publicationJobId"] = id
            state.update { it.copy(jobId = id) }
            val payment = repository.payment(id)
            state.update { it.copy(payment = payment) }
        }
    }
    fun resume() {
        val id = state.value[PublicationField.RESUME_ID].toLongOrNull()?.takeIf { it > 0 } ?: return
        action {
            val payment = repository.payment(id)
            saved["publicationJobId"] = id
            state.update { it.copy(jobId = id, payment = payment) }
        }
    }
    fun refresh() {
        val id = state.value.jobId ?: return
        action { state.update { it.copy(payment = null) }; val payment = repository.payment(id); state.update { it.copy(payment = payment) } }
    }
    private fun action(block: suspend () -> Unit) {
        if (state.value.busy) return
        state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try { block() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { state.update { it.copy(error = backend.message(e)) } }
            finally { state.update { it.copy(busy = false) } }
        }
    }
}
