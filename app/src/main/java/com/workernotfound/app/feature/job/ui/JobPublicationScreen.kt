package com.workernotfound.app.feature.job.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.*
import com.workernotfound.app.core.maps.AppMap
import com.workernotfound.app.core.maps.MapPoint
import com.workernotfound.app.feature.job.viewmodel.*

@Composable
fun JobPublicationScreen(onBack: () -> Unit, viewModel: JobPublicationViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(AppColors.Surface).safeDrawingPadding()) {
        AppTopBar(stringResource(if (state.jobId == null) R.string.publication_title else R.string.publication_payment_title), onBack = onBack)
        Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(AppDimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
            state.error?.let { Text(it, color = AppColors.Danger) }
            if (state.busy) CircularProgressIndicator(color = AppTheme.role.accent)
            if (state.jobId != null) {
                PublicationResult(state, viewModel::refresh)
            } else {
                PublicationForm(state, viewModel)
            }
        }
    }
}

@Composable
private fun PublicationResult(state: JobPublicationUiState, onRefresh: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.publication_created), style = MaterialTheme.typography.titleMedium)
        state.payment?.let { payment ->
            Text(stringResource(if (payment.isPublished) R.string.publication_open else R.string.publication_not_open))
            payment.amount?.let { Text(stringResource(R.string.publication_amount, it.toPlainString(), payment.currency.orEmpty())) }
            if (payment.changePending) Text(stringResource(R.string.publication_change_pending))
        }
    }
    if (state.payment?.isPublished != true) {
        com.workernotfound.app.feature.payment.ui.PublicationCheckout(
            jobId = requireNotNull(state.jobId), payment = state.payment, enabled = !state.busy, onUpdated = onRefresh)
        AppPrimaryButton(stringResource(R.string.publication_refresh), onRefresh, enabled = !state.busy)
    }
}

@Composable
private fun PublicationForm(state: JobPublicationUiState, viewModel: JobPublicationViewModel) {
    val enabled = !state.busy && !state.creationAttempted
    PublicationSection(R.string.publication_section_store) {
        val business = state.business
        if (business == null) {
            Text(stringResource(R.string.publication_business_required))
            TextButton(viewModel::loadBusiness, enabled = enabled) { Text(stringResource(R.string.publication_business_reload)) }
        } else {
            Text(business.name, style = MaterialTheme.typography.titleMedium)
            Text(business.address)
            AppMap(listOf(MapPoint(business.id.toString(), business.latitude.toDouble(), business.longitude.toDouble(), business.name)),
                Modifier.fillMaxWidth().height(AppDimens.locationMapHeight))
        }
    }
    PublicationSection(R.string.publication_section_schedule) {
        PublicationInput(PublicationField.DATE, state, viewModel::edit)
        Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
            Box(Modifier.weight(1f)) { PublicationInput(PublicationField.START, state, viewModel::edit) }
            Box(Modifier.weight(1f)) { PublicationInput(PublicationField.END, state, viewModel::edit) }
        }
        PublicationInput(PublicationField.DEADLINE, state, viewModel::edit)
    }
    PublicationSection(R.string.publication_section_work) {
        PublicationInput(PublicationField.TITLE, state, viewModel::edit)
        PublicationInput(PublicationField.DESCRIPTION, state, viewModel::edit)
        if (state.categories.isEmpty()) {
            Text(stringResource(R.string.publication_category_help), color = AppColors.TextSub)
            TextButton(viewModel::loadCategories, enabled = enabled) { Text(stringResource(R.string.publication_category_reload)) }
        } else if (!state.categoryFromProfile) PublicationCategoryPicker(state.categories, state.selectedCategory, enabled, viewModel::category)
        PublicationInput(PublicationField.COUNT, state, viewModel::edit)
    }
    PublicationSection(R.string.publication_section_wage) {
        PublicationInput(PublicationField.BASE_WAGE, state, viewModel::edit)
        PublicationInput(PublicationField.EXTRA_WAGE, state, viewModel::edit)
        Text(stringResource(R.string.publication_urgency), style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
            listOf("LOW", "MEDIUM", "HIGH").forEachIndexed { i, value ->
                val labels = listOf(R.string.publication_low, R.string.publication_medium, R.string.publication_high)
                FilterChip(state.urgency == value, { viewModel.urgency(value) }, label = { Text(stringResource(labels[i])) }, enabled = enabled)
            }
        }
    }
    if (state.creationAttempted) Text(stringResource(R.string.publication_uncertain), color = AppColors.Danger)
    else state.validationIssue?.let { Text(stringResource(it.messageResource()), color = AppColors.Danger) }
    AppPrimaryButton(stringResource(R.string.publication_create), viewModel::create, enabled = state.canCreate)
}

@Composable
private fun PublicationSection(title: Int, content: @Composable ColumnScope.() -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
            content()
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
    }
    val enabled = !state.busy && !state.creationAttempted
    if (field in setOf(PublicationField.DATE, PublicationField.START, PublicationField.END, PublicationField.DEADLINE)) {
        PublicationScheduleInput(field, state[field], stringResource(label), enabled, state[PublicationField.START],
            state[PublicationField.DATE]) { edit(field, it) }
        return
    }
    val numeric = field in setOf(PublicationField.BASE_WAGE, PublicationField.EXTRA_WAGE, PublicationField.COUNT)
    OutlinedTextField(state[field], { value ->
        if (!numeric || value.all { it in '0'..'9' }) edit(field, if (field == PublicationField.DESCRIPTION) value.take(200) else value)
    }, Modifier.fillMaxWidth(), label = { Text(stringResource(label)) }, enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text),
        supportingText = if (field == PublicationField.DESCRIPTION) {{ Text(stringResource(R.string.publication_description_count, state[field].length)) }} else null,
        minLines = if (field == PublicationField.DESCRIPTION) 3 else 1,
        singleLine = field != PublicationField.DESCRIPTION)
}

private fun PublicationValidationIssue.messageResource() = when (this) {
    PublicationValidationIssue.WAGE -> R.string.publication_invalid_wage
    PublicationValidationIssue.EXTRA_WAGE -> R.string.publication_invalid_extra
    PublicationValidationIssue.COUNT -> R.string.publication_invalid_count
    PublicationValidationIssue.SCHEDULE -> R.string.publication_invalid_schedule
    PublicationValidationIssue.DEADLINE -> R.string.publication_invalid_deadline
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
