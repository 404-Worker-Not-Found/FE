package com.workernotfound.app.feature.worker.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.feature.worker.domain.model.ApplyStatus
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting

/** Presentation priority only: use the repository order; never re-sort by wage. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkerFocusJobCard(job: WorkerJobPosting, onClick: () -> Unit, onApply: () -> Unit,
    modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium, color = AppColors.FocusSurface,
        contentColor = AppColors.FocusContent) {
        Column(Modifier.padding(AppDimens.spaceXl)) {
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(AppDimens.spaceXs)) {
                Text(stringResource(if (job.isUrgent) R.string.design_focus_urgent
                    else R.string.design_focus_featured), style = MaterialTheme.typography.labelMedium,
                    color = AppColors.FocusSub)
                job.deadlineText?.let { Text(it, style = MaterialTheme.typography.labelMedium,
                    color = AppColors.FocusDeadline) }
            }
            Spacer(Modifier.height(AppDimens.spaceLg))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppDimens.spaceXs),
                verticalArrangement = Arrangement.spacedBy(AppDimens.spaceXs)) {
                Text(stringResource(R.string.design_wage, job.hourlyWage),
                    style = MaterialTheme.typography.displaySmall)
                Text(stringResource(R.string.design_focus_hour_unit),
                    modifier = Modifier.align(Alignment.CenterVertically),
                    style = MaterialTheme.typography.bodySmall, color = AppColors.FocusSub)
            }
            Text(job.storeName, style = MaterialTheme.typography.titleMedium)
            Text(job.category, style = MaterialTheme.typography.bodySmall, color = AppColors.FocusSub)
            Spacer(Modifier.height(AppDimens.spaceLg))
            HorizontalDivider(color = AppColors.FocusOutline)
            Spacer(Modifier.height(AppDimens.spaceMd))
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimens.spaceLg),
                verticalArrangement = Arrangement.spacedBy(AppDimens.spaceMd)) {
                FocusMetadata(stringResource(R.string.design_focus_work_time), job.workTime,
                    Modifier.weight(1f).widthIn(min = AppDimens.focusMetadataMinWidth))
                FocusMetadata(stringResource(R.string.design_focus_distance), job.distanceText,
                    Modifier.weight(1f).widthIn(min = AppDimens.focusMetadataMinWidth))
            }
            Spacer(Modifier.height(AppDimens.spaceLg))
            FocusApplyButton(job.applyStatus, onApply)
        }
    }
}

@Composable
private fun FocusMetadata(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppDimens.spaceXs)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.FocusSub)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = AppColors.FocusContent)
    }
}

@Composable
private fun FocusApplyButton(status: ApplyStatus, onClick: () -> Unit) {
    val applied = status == ApplyStatus.APPLIED
    Button(onClick = onClick, enabled = !applied,
        modifier = Modifier.fillMaxWidth().heightIn(min = AppDimens.touchTarget),
        shape = MaterialTheme.shapes.small, colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.role.accent, contentColor = AppTheme.role.onAccent,
            disabledContainerColor = AppColors.SuccessBg, disabledContentColor = AppColors.Success)) {
        Text(stringResource(if (applied) R.string.design_applied else R.string.design_apply),
            style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(AppDimens.iconSize))
    }
}

/** Compact wage-first row; direct apply remains available alongside the detail action. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkerFocusJobRow(job: WorkerJobPosting, onClick: () -> Unit, onApply: () -> Unit,
    modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().clickable(onClick = onClick)) {
        FlowRow(Modifier.fillMaxWidth().padding(vertical = AppDimens.spaceLg),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spaceSm)) {
            JobCategoryIcon(job.category)
            Column(Modifier.weight(1f).widthIn(min = AppDimens.focusMetadataMinWidth)) {
                Text(job.storeName, style = MaterialTheme.typography.titleMedium, color = AppColors.TextMain)
                Text(job.workTime, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSub)
                Text(stringResource(R.string.design_job_metadata, job.category, job.distanceText),
                    style = MaterialTheme.typography.bodySmall, color = AppColors.TextSub)
                job.deadlineText?.let { Text(it, style = MaterialTheme.typography.labelSmall,
                    color = AppColors.Danger) }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(R.string.design_per_hour), style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSub)
                Text(stringResource(R.string.design_wage, job.hourlyWage),
                    style = MaterialTheme.typography.titleMedium, color = AppColors.TextMain)
                WorkerApplyButton(job.applyStatus, onApply)
            }
        }
        HorizontalDivider(color = AppColors.Border)
    }
}

@Composable
private fun JobCategoryIcon(category: String) {
    val icon = when {
        category.contains("카페") || category.contains("베이커리") -> Icons.Outlined.LocalCafe
        category.contains("음식") -> Icons.Outlined.Restaurant
        else -> Icons.Outlined.Storefront
    }
    Surface(color = AppTheme.role.accentTint, shape = MaterialTheme.shapes.small) {
        Box(Modifier.size(AppDimens.categoryIconSize), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(AppDimens.iconSize), tint = AppTheme.role.accentPressed)
        }
    }
}
