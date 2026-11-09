package com.workernotfound.app.feature.review.ui

import com.workernotfound.app.core.ui.KoreanDateTime

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.*

@Composable
fun ReviewScreen(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: ReviewViewModel = hiltViewModel()) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier.fillMaxSize().statusBarsPadding()) {
        AppTopBar(stringResource(R.string.reviews_title), onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(AppDimens.screenPadding), verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
            item {
                TextButton(viewModel::load, enabled = !s.busy) { Text(stringResource(R.string.reviews_refresh)) }
                if (s.busy) CircularProgressIndicator()
                s.error?.let { Text(it, color = AppColors.Danger) }
                if (s.written) Text(stringResource(R.string.reviews_written), color = AppColors.Success)
                SectionLabel(stringResource(R.string.reviews_pending))
                if (!s.busy && s.pending.isEmpty()) Text(stringResource(R.string.reviews_no_pending))
            }
            items(s.pending, key = { "pending-${it.workId}" }) { target ->
                AppCard(Modifier.fillMaxWidth()) {
                    Text(target.storeName, style = MaterialTheme.typography.titleMedium)
                    Text(target.description)
                    Text(target.date, color = AppColors.TextSub)
                    Text(stringResource(R.string.reviews_until, KoreanDateTime.dateTime(target.writableUntil)), color = AppColors.TextSub)
                    TextButton({ viewModel.select(target) }, enabled = !s.busy) { Text(stringResource(R.string.reviews_write)) }
                }
            }
            if (s.pendingNext != null) item { TextButton({ viewModel.more(true) }, enabled = !s.busy) { Text(stringResource(R.string.reviews_more_pending)) } }
            item {
                SectionLabel(stringResource(R.string.reviews_received))
                Text(stringResource(R.string.reviews_count, s.count))
                s.average?.let { Text(stringResource(R.string.reviews_average, it.stripTrailingZeros().toPlainString())) }
                if (!s.busy && s.received.isEmpty()) Text(stringResource(R.string.reviews_no_received))
            }
            items(s.received, key = { "received-${it.id}" }) { review ->
                AppCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.reviews_rating, review.rating))
                    Text(stringResource(if (review.role == "OWNER") R.string.reviews_from_owner else R.string.reviews_from_worker))
                    review.comment?.let { Text(it) }
                    Text(KoreanDateTime.dateTime(review.createdAt), color = AppColors.TextSub)
                }
            }
            if (s.receivedNext != null) item { TextButton({ viewModel.more(false) }, enabled = !s.busy) { Text(stringResource(R.string.reviews_more_received)) } }
        }
    }
    s.selected?.let { target ->
        AlertDialog(onDismissRequest = { viewModel.select(null) }, title = { Text(stringResource(R.string.reviews_write)) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
                Text(target.storeName); Text(target.date)
                ChoiceChipRow((1..5).map { stringResource(R.string.reviews_rating, it) }, s.rating - 1, { viewModel.rating(it + 1) })
                OutlinedTextField(s.comment, viewModel::comment, enabled = !s.busy && !s.attempted,
                    label = { Text(stringResource(R.string.reviews_comment)) }, isError = s.comment.length > 500)
                if (s.attempted) Text(stringResource(R.string.reviews_retry_notice), color = AppColors.TextSub)
                s.error?.let { Text(it, color = AppColors.Danger) }
            } },
            confirmButton = { TextButton(viewModel::write, enabled = !s.busy && s.rating in 1..5 && s.comment.length <= 500) { Text(stringResource(R.string.reviews_submit)) } },
            dismissButton = { TextButton({ viewModel.select(null) }, enabled = !s.busy) { Text(stringResource(R.string.reviews_close)) } })
    }
}
