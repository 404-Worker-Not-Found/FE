package com.workernotfound.app.feature.job.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.*
import com.workernotfound.app.feature.job.viewmodel.*

@Composable
fun JobPublicationScreen(onBack: () -> Unit, viewModel: JobPublicationViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(AppColors.Surface).safeDrawingPadding()) {
        AppTopBar(stringResource(R.string.publication_title), onBack = onBack)
        Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(AppDimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
            state.error?.let { Text(it, color = AppColors.Danger) }
            if (state.busy) CircularProgressIndicator()
            if (state.jobId != null) {
                Text(stringResource(R.string.publication_created, requireNotNull(state.jobId)))
                state.payment?.let { payment ->
                    Text(stringResource(if (payment.isPublished) R.string.publication_open else R.string.publication_not_open))
                    Text(stringResource(R.string.publication_status, payment.jobStatus, payment.creationStatus, payment.orderStatus.orEmpty()))
                    payment.orderId?.let { Text(stringResource(R.string.publication_order, it)) }
                    payment.amount?.let { Text(stringResource(R.string.publication_amount, it.toPlainString(), payment.currency.orEmpty())) }
                    if (payment.changePending) Text(stringResource(R.string.publication_change_pending))
                }
                com.workernotfound.app.feature.payment.ui.PublicationCheckout(
                    jobId = requireNotNull(state.jobId), payment = state.payment, enabled = !state.busy,
                    onUpdated = viewModel::refresh)
                AppPrimaryButton(stringResource(R.string.publication_refresh), viewModel::refresh, enabled = !state.busy)
            } else {
                if (state.business == null) {
                    Text(stringResource(R.string.publication_business_required))
                    TextButton(viewModel::loadBusiness, enabled = !state.busy) { Text(stringResource(R.string.publication_business_reload)) }
                } else {
                    Text(requireNotNull(state.business).name, style = MaterialTheme.typography.titleMedium)
                    val business = requireNotNull(state.business)
                    com.workernotfound.app.feature.location.LocationPicker(
                        address = business.address,
                        latitude = if (state.locationSelected) business.latitude.toPlainString() else "",
                        longitude = if (state.locationSelected) business.longitude.toPlainString() else "",
                        enabled = !state.busy && !state.creationAttempted,
                        onAddress = viewModel::address, onPick = viewModel::location)
                    Text(stringResource(R.string.publication_business_id, requireNotNull(state.business).id))
                }
                if (state.creationAttempted) Text(stringResource(R.string.publication_uncertain), color = AppColors.Danger)
                PublicationField.entries.filter { it != PublicationField.RESUME_ID }.forEach { field ->
                    PublicationInput(field, state, viewModel::edit)
                }
                if (state.categories.isEmpty()) {
                    Text(stringResource(R.string.publication_category_help), color = AppColors.TextSub)
                    TextButton(viewModel::loadCategories, enabled = !state.busy && !state.creationAttempted) {
                        Text(stringResource(R.string.publication_category_reload))
                    }
                } else {
                    PublicationCategoryPicker(state.categories, state.selectedCategory,
                        enabled = !state.busy && !state.creationAttempted, onSelect = viewModel::category)
                }
                Row { Checkbox(state.nextDay, viewModel::nextDay, enabled = !state.busy && !state.creationAttempted); Text(stringResource(R.string.publication_next_day)) }
                val urgencies = listOf("LOW", "MEDIUM", "HIGH")
                val labels = listOf(R.string.publication_low, R.string.publication_medium, R.string.publication_high)
                Row {
                    urgencies.forEachIndexed { i, value ->
                        FilterChip(state.urgency == value, { viewModel.urgency(value) }, label = { Text(stringResource(labels[i])) }, enabled = !state.busy && !state.creationAttempted)
                    }
                }
                Text(stringResource(R.string.publication_rules), color = AppColors.TextSub)
                AppPrimaryButton(stringResource(R.string.publication_create), viewModel::create, enabled = state.canCreate)
                HorizontalDivider()
                PublicationInput(PublicationField.RESUME_ID, state, viewModel::edit)
                TextButton(viewModel::resume, enabled = !state.busy && (state[PublicationField.RESUME_ID].toLongOrNull() ?: 0) > 0) {
                    Text(stringResource(R.string.publication_resume))
                }
            }
            TextButton(onBack) { Text(stringResource(R.string.publication_back)) }
        }
    }
}

@Composable
private fun PublicationInput(field: PublicationField, state: JobPublicationUiState, edit: (PublicationField, String) -> Unit) {
    val label = when (field) {
        PublicationField.TITLE -> R.string.publication_job_title; PublicationField.DESCRIPTION -> R.string.publication_description
        PublicationField.DATE -> R.string.publication_date
        PublicationField.START -> R.string.publication_start; PublicationField.END -> R.string.publication_end
        PublicationField.BASE_WAGE -> R.string.publication_base_wage; PublicationField.EXTRA_WAGE -> R.string.publication_extra_wage
        PublicationField.COUNT -> R.string.publication_count; PublicationField.DEADLINE -> R.string.publication_deadline
        PublicationField.RESUME_ID -> R.string.publication_resume_id
    }
    if (field in setOf(PublicationField.DATE, PublicationField.START, PublicationField.END, PublicationField.DEADLINE)) {
        PublicationScheduleInput(field, state[field], stringResource(label), !state.busy && !state.creationAttempted) { edit(field, it) }
        return
    }
    OutlinedTextField(state[field], { edit(field, it) }, Modifier.fillMaxWidth(),
        label = { Text(stringResource(label)) }, enabled = !state.busy && (!state.creationAttempted || field == PublicationField.RESUME_ID),
        singleLine = field != PublicationField.DESCRIPTION)
}

@Composable
private fun PublicationCategoryPicker(categories: List<com.workernotfound.app.feature.job.domain.model.PostingCategory>,
    selected: com.workernotfound.app.feature.job.domain.model.PostingCategory?, enabled: Boolean,
    onSelect: (com.workernotfound.app.feature.job.domain.model.PostingCategory) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton({ expanded = true }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Text(selected?.name ?: stringResource(R.string.publication_category_select))
        }
        DropdownMenu(expanded && enabled, onDismissRequest = { expanded = false }) {
            categories.forEach { category -> DropdownMenuItem(text = { Text(category.name) },
                onClick = { onSelect(category); expanded = false }) }
        }
    }
}
