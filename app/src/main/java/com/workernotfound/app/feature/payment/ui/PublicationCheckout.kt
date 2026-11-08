package com.workernotfound.app.feature.payment.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.feature.job.domain.model.PublicationPayment

@Composable
fun PublicationCheckout(jobId: Long, payment: PublicationPayment?, enabled: Boolean,
    onUpdated: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: CheckoutViewModel = hiltViewModel(key = "checkout-$jobId")
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val updated by rememberUpdatedState(onUpdated)
    LaunchedEffect(state.revision) { if (state.revision > 0) updated() }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
        Text(stringResource(R.string.checkout_test_notice), color = AppColors.TextSub)
        if (payment?.jobStatus == "PAYMENT_PENDING" && payment.creationStatus == "CREATED" &&
            payment.orderStatus == "READY" && !payment.changePending && !state.retryConfirmation) {
            AppPrimaryButton(stringResource(R.string.checkout_open), { viewModel.open(jobId) }, enabled = enabled && !state.busy)
        }
        if (state.busy) { CircularProgressIndicator(); Text(stringResource(R.string.checkout_processing)) }
        if (state.failed) Text(stringResource(R.string.checkout_canceled), color = AppColors.TextSub)
        state.error?.let { Text(it, color = AppColors.Danger) }
        if (state.retryConfirmation) {
            Text(stringResource(R.string.checkout_uncertain), color = AppColors.TextSub)
            TextButton(viewModel::retry, enabled = !state.busy) { Text(stringResource(R.string.checkout_retry)) }
        }
    }
    state.checkout?.let { CheckoutWebView(it, viewModel::result, viewModel::cancel, viewModel::invalid) }
}
