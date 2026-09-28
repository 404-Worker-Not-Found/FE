package com.workernotfound.app.feature.owner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppRadius
import com.workernotfound.app.core.designsystem.AppTheme

/** Distance at which the worker dot reaches the edge of the mini-map. */
private const val MAP_RANGE_METERS = 2_500f

/**
 * 실시간 위치 (UI spec 2-4). Style-guide custom map view: store pin in the owner
 * accent at the center, worker location as a blue dot offset by distance. The live
 * Naver map is not wired yet (decision: Map SDK: Naver Map — client id not provided),
 * matching the worker screens' placeholder approach.
 */
@Composable
internal fun OwnerLiveLocationMap(
    distanceMeters: Int,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(AppColors.MapBackground, RoundedCornerShape(AppRadius.inner)),
    ) {
        MapDecorations()
        val ratio = (distanceMeters / MAP_RANGE_METERS).coerceIn(0f, 1f)
        val workerX = (maxWidth / 2 - 24.dp) * ratio
        val workerY = (maxHeight / 2 - 24.dp) * ratio
        Pin(color = AppTheme.role.accent, size = 22.dp, modifier = Modifier.align(Alignment.Center))
        Pin(
            color = AppColors.WorkerLocationDot,
            size = 14.dp,
            modifier = Modifier.align(Alignment.Center).offset(x = -workerX, y = workerY),
        )
        Text(
            text = distanceCaption(distanceMeters),
            color = AppColors.TextMain,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .background(AppColors.CardSurface, RoundedCornerShape(AppRadius.pill))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun MapDecorations() {
    Box(Modifier.offset(16.dp, 16.dp).size(90.dp, 50.dp).background(AppColors.MapBlock, RoundedCornerShape(8.dp)))
    Box(Modifier.offset(200.dp, 24.dp).size(70.dp, 90.dp).background(AppColors.MapBlock, RoundedCornerShape(8.dp)))
    Box(Modifier.offset(120.dp, 120.dp).size(110.dp, 44.dp).background(AppColors.MapPark, RoundedCornerShape(8.dp)))
}

@Composable
private fun Pin(color: Color, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .border(2.dp, AppColors.CardSurface, CircleShape)
            .background(color, CircleShape),
    )
}

private fun distanceCaption(distanceMeters: Int): String = when {
    distanceMeters <= 0 -> "알바생이 매장에 도착했어요"
    distanceMeters < 1_000 -> "매장까지 약 ${distanceMeters}m"
    else -> "매장까지 약 %.1fkm".format(distanceMeters / 1_000f)
}
