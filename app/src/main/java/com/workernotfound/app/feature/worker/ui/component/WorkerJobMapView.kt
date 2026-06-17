package com.workernotfound.app.feature.worker.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppRadius
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting

/**
 * Custom map placeholder for job search (UI spec 3-2: 지도 보기).
 *
 * This is the style-guide "custom map view", not a real map — the demo has no
 * Naver Map client id. Wage bubbles act as pins; tapping one shows a preview
 * card (decision: Map SDK: Naver Map — custom view used for the demo placeholder).
 */
@Composable
fun WorkerJobMapView(
    jobs: List<WorkerJobPosting>,
    selectedJobId: String?,
    onSelectPin: (String) -> Unit,
    onJobClick: (String) -> Unit,
    onApply: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(AppColors.MapBackground)) {
        // Decorative blocks / park to suggest a map (style guide map colors).
        Box(Modifier.offset(24.dp, 40.dp).size(110.dp, 70.dp).background(AppColors.MapBlock, RoundedCornerShape(8.dp)))
        Box(Modifier.offset(170.dp, 90.dp).size(90.dp, 120.dp).background(AppColors.MapBlock, RoundedCornerShape(8.dp)))
        Box(Modifier.offset(40.dp, 200.dp).size(120.dp, 80.dp).background(AppColors.MapPark, RoundedCornerShape(8.dp)))

        // Worker's current location (style guide: 알바생 위치 dot).
        Box(
            Modifier.align(Alignment.Center).size(16.dp)
                .background(AppColors.WorkerLocationDot, CircleShape),
        )

        // Wage-bubble pins, placed deterministically by index.
        jobs.forEachIndexed { index, job ->
            val x = (20 + (index % 3) * 104).dp
            val y = (52 + (index / 3) * 96).dp
            WageBubblePin(
                job = job,
                selected = job.id == selectedJobId,
                onClick = { onSelectPin(job.id) },
                modifier = Modifier.offset(x, y),
            )
        }

        // Preview card for the selected pin (UI spec 3-2: 탭 시 미리보기 카드 팝업).
        val selected = jobs.firstOrNull { it.id == selectedJobId }
        if (selected != null) {
            WorkerRecommendedJobCard(
                job = selected,
                onClick = { onJobClick(selected.id) },
                onApply = { onApply(selected.id) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(AppDimens.screenPadding),
            )
        }
    }
}

@Composable
private fun WageBubblePin(
    job: WorkerJobPosting,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) AppColors.WorkerEmphasis else AppColors.Worker
    Text(
        text = (if (job.isUrgent) "⚡ " else "") + "₩${"%,d".format(job.hourlyWage)}",
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(bg, RoundedCornerShape(AppRadius.pill))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}
