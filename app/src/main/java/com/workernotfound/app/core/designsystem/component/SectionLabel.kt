package com.workernotfound.app.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.workernotfound.app.core.designsystem.AppColors

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier.semantics { heading() },
        color = AppColors.TextMain, style = MaterialTheme.typography.titleMedium)
}
