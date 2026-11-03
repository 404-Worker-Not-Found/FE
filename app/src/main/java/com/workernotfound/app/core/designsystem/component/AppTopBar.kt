package com.workernotfound.app.core.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*

/** A leading title gives content and navigation a stable shared alignment. */
@Composable
fun AppTopBar(title: String, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null) {
    Surface(color = AppColors.CardSurface) {
        Row(modifier.fillMaxWidth().heightIn(min = AppDimens.buttonHeight)
            .padding(horizontal = AppDimens.spaceXs, vertical = AppDimens.spaceXs),
            verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.design_back))
            } else Spacer(Modifier.width(AppDimens.spaceLg))
            Text(title, Modifier.weight(1f).padding(end = AppDimens.screenPadding)
                .semantics { heading() }, style = MaterialTheme.typography.titleLarge)
        }
    }
}
