package com.workernotfound.app.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.workernotfound.app.core.designsystem.AppColors

/** Placeholder for demo screens that are not built yet. */
@Composable
fun ComingSoonScreen(title: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "$title\n(준비 중)",
            color = AppColors.TextSub,
            textAlign = TextAlign.Center,
        )
    }
}
