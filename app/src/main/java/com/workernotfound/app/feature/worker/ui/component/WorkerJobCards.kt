package com.workernotfound.app.feature.worker.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.*
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting

@Composable
private fun JobIdentity(job: WorkerJobPosting) {
    Text(job.storeName, style = MaterialTheme.typography.titleMedium, color = AppColors.TextMain)
    Text(stringResource(R.string.design_job_metadata, job.category, job.distanceText),
        style = MaterialTheme.typography.bodySmall, color = AppColors.TextSub)
    Text(job.workTime, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSub)
}

@Composable
private fun JobActionRow(job: WorkerJobPosting, onApply: () -> Unit) {
    // A column keeps long wage values and scaled text clear of the apply target.
    FlowJobAction(job, onApply)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowJobAction(job: WorkerJobPosting, onApply: () -> Unit) {
    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(AppDimens.spaceSm)) {
        Column(Modifier.padding(end = AppDimens.spaceLg)) {
            Text(stringResource(R.string.design_per_hour), style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextSub)
            Text(stringResource(R.string.design_wage, job.hourlyWage),
                style = MaterialTheme.typography.titleLarge, color = AppColors.TextMain)
        }
        WorkerApplyButton(job.applyStatus, onApply)
    }
}

/** Only time-sensitive jobs receive contained emphasis. Width comes from the caller. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkerUrgentJobCard(job: WorkerJobPosting, onClick: () -> Unit, onApply: () -> Unit,
    modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        containerColor = AppTheme.role.accentTint, borderColor = AppTheme.role.accentTint) {
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimens.spaceSm),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spaceXs)) {
            UrgentBadge()
            job.deadlineText?.let { Text(it, color = AppColors.Danger,
                style = MaterialTheme.typography.labelMedium) }
        }
        Spacer(Modifier.height(AppDimens.spaceMd))
        JobIdentity(job)
        Spacer(Modifier.height(AppDimens.spaceLg))
        JobActionRow(job, onApply)
    }
}

/** A continuous list row, with typography and a divider instead of another card. */
@Composable
fun WorkerRecommendedJobCard(job: WorkerJobPosting, onClick: () -> Unit, onApply: () -> Unit,
    modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(vertical = AppDimens.spaceMd)) {
            JobIdentity(job)
            Spacer(Modifier.height(AppDimens.spaceMd))
            JobActionRow(job, onApply)
        }
        HorizontalDivider(color = AppColors.Border)
    }
}
