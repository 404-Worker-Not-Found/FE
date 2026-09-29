package com.workernotfound.app.feature.owner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppDimens
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppPrimaryButton
import com.workernotfound.app.core.designsystem.component.NoShowRisk
import com.workernotfound.app.feature.owner.domain.model.NoShowRiskLevel

@Composable
internal fun OwnerLoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppTheme.role.accent)
    }
}

@Composable
internal fun OwnerMessageBox(
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxSize().padding(AppDimens.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = message, color = AppColors.TextSub, textAlign = TextAlign.Center)
        if (actionText != null) {
            Spacer(Modifier.height(12.dp))
            AppPrimaryButton(text = actionText, onClick = onAction)
        }
    }
}

/** Label / value row used inside cards. */
@Composable
internal fun OwnerInfoLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = AppColors.TextMain,
    isEmphasized: Boolean = false,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, color = AppColors.TextSub, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            color = valueColor,
            fontSize = if (isEmphasized) 18.sp else 13.sp,
            fontWeight = if (isEmphasized) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

/** Profile image, or the first letter of the name when no image is available. */
@Composable
internal fun ProfileAvatar(
    name: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val avatarModifier = modifier.size(size).clip(CircleShape)
    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "$name 프로필 이미지",
            contentScale = ContentScale.Crop,
            modifier = avatarModifier,
        )
    } else {
        Box(
            modifier = avatarModifier.background(AppTheme.role.accentTint),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.take(1),
                color = AppTheme.role.accent,
                fontSize = (size.value * 0.4f).sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

internal fun NoShowRiskLevel.toBadge(): NoShowRisk = when (this) {
    NoShowRiskLevel.LOW -> NoShowRisk.LOW
    NoShowRiskLevel.MIDDLE -> NoShowRisk.MIDDLE
    NoShowRiskLevel.HIGH -> NoShowRisk.HIGH
}
