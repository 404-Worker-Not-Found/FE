package com.workernotfound.app.core.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*

@Composable
fun HomeBrandHeader(title: String, subtitle: String, modifier: Modifier = Modifier,
    onNotificationsClick: (() -> Unit)? = null) {
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spaceSm)) {
            Surface(color = AppTheme.role.accent, shape = RoundedCornerShape(AppRadius.inner)) {
                Box(Modifier.size(AppDimens.spaceXl), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Bolt, null, Modifier.size(AppDimens.spaceLg), tint = AppTheme.role.onAccent)
                }
            }
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium,
                color = AppTheme.role.accentPressed)
            Spacer(Modifier.weight(1f))
            if (onNotificationsClick != null) IconButton(onClick = onNotificationsClick) {
                Icon(Icons.Outlined.Notifications, stringResource(R.string.design_applications))
            }
        }
        Spacer(Modifier.height(AppDimens.spaceXl))
        Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineMedium,
            color = AppColors.TextMain)
        Spacer(Modifier.height(AppDimens.spaceXs))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSub)
    }
}
