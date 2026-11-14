package com.workernotfound.app.feature.review.ui

import com.workernotfound.app.core.ui.KoreanDateTime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.*
import com.workernotfound.app.feature.review.domain.ReceivedReview
import com.workernotfound.app.feature.review.domain.ReviewTarget
import java.math.BigDecimal

/** 내 리뷰: 요약 → 작성 가능한 리뷰 → 받은 리뷰. 새로고침은 상단 바 아이콘. Shared by both roles. */
@Composable
fun ReviewScreen(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: ReviewViewModel = hiltViewModel()) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier.fillMaxSize().background(AppTheme.role.screenBackground).statusBarsPadding()) {
        AppTopBar(
            stringResource(R.string.reviews_title),
            onBack = onBack,
            actions = {
                IconButton(onClick = viewModel::load, enabled = !s.busy) {
                    Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.reviews_refresh), tint = AppColors.TextSub)
                }
            },
        )
        val firstLoad = s.busy && s.pending.isEmpty() && s.received.isEmpty()
        if (firstLoad) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AppTheme.role.accent) }
            return@Column
        }
        LazyColumn(contentPadding = PaddingValues(AppDimens.screenPadding), verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
            item {
                s.error?.let { Text(it, color = AppColors.Danger, fontSize = 13.sp, modifier = Modifier.padding(bottom = 4.dp)) }
                if (s.written) Text(stringResource(R.string.reviews_written), color = AppColors.Success, fontSize = 13.sp)
                SummaryCard(count = s.count, average = s.average)
            }
            item { SectionLabel(stringResource(R.string.reviews_pending), modifier = Modifier.padding(top = 8.dp)) }
            if (!s.busy && s.pending.isEmpty()) item { EmptyCard(stringResource(R.string.reviews_no_pending)) }
            items(s.pending, key = { "pending-${it.workId}" }) { target ->
                PendingCard(target, enabled = !s.busy, onWrite = { viewModel.select(target) })
            }
            if (s.pendingNext != null) item {
                MoreButton(stringResource(R.string.reviews_more_pending), enabled = !s.busy) { viewModel.more(true) }
            }
            item { SectionLabel(stringResource(R.string.reviews_received), modifier = Modifier.padding(top = 8.dp)) }
            if (!s.busy && s.received.isEmpty()) item { EmptyCard(stringResource(R.string.reviews_no_received)) }
            items(s.received, key = { "received-${it.id}" }) { review -> ReceivedCard(review) }
            if (s.receivedNext != null) item {
                MoreButton(stringResource(R.string.reviews_more_received), enabled = !s.busy) { viewModel.more(false) }
            }
        }
    }
    s.selected?.let { target ->
        AlertDialog(onDismissRequest = { viewModel.select(null) }, title = { Text(stringResource(R.string.reviews_write)) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(AppDimens.cardGap)) {
                Text(target.storeName); Text(KoreanDateTime.workDate(target.date))
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

@Composable
private fun SummaryCard(count: Long, average: BigDecimal?) {
    AppCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.reviews_received), fontSize = 13.sp, color = AppColors.TextSub)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.reviews_count, count), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = average?.setScale(1, java.math.RoundingMode.HALF_UP)?.toPlainString() ?: "-",
                    fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppTheme.role.accentPressed,
                )
                Text(stars(average?.toDouble()?.let { kotlin.math.round(it).toInt() } ?: 0), fontSize = 13.sp, color = AppTheme.role.accent)
            }
        }
    }
}

@Composable
private fun PendingCard(target: ReviewTarget, enabled: Boolean, onWrite: () -> Unit) {
    AppCard(Modifier.fillMaxWidth()) {
        Text(target.storeName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
        if (target.description.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(target.description, fontSize = 13.sp, color = AppColors.TextSub, maxLines = 2)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(KoreanDateTime.workDate(target.date), fontSize = 12.sp, color = AppColors.TextSub)
                Text(stringResource(R.string.reviews_until, KoreanDateTime.dateTime(target.writableUntil)), fontSize = 12.sp, color = AppColors.Danger)
            }
            Button(
                onClick = onWrite, enabled = enabled,
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.role.accent),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            ) { Text(stringResource(R.string.reviews_write), fontSize = 13.sp) }
        }
    }
}

@Composable
private fun ReceivedCard(review: ReceivedReview) {
    AppCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stars(review.rating), fontSize = 15.sp, color = AppTheme.role.accent)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.reviews_rating, review.rating), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(if (review.role == "OWNER") R.string.reviews_from_owner else R.string.reviews_from_worker),
                fontSize = 11.sp, color = AppTheme.role.accentPressed,
                modifier = Modifier.background(AppTheme.role.accentTint, RoundedCornerShape(AppRadius.pill))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        review.comment?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, fontSize = 14.sp, lineHeight = 20.sp, color = AppColors.TextMain)
        }
        Spacer(Modifier.height(8.dp))
        Text(KoreanDateTime.workDate(review.createdAt), fontSize = 12.sp, color = AppColors.TextPlaceholder)
    }
}

@Composable
private fun EmptyCard(text: String) {
    AppCard(Modifier.fillMaxWidth()) {
        Text(text, fontSize = 13.sp, color = AppColors.TextSub, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun MoreButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    AppSecondaryButton(text, onClick, enabled = enabled)
}

private fun stars(rating: Int): String = "★".repeat(rating.coerceIn(0, 5)) + "☆".repeat(5 - rating.coerceIn(0, 5))
