package com.workernotfound.app.feature.owner.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppTheme

/** Owner-styled alert dialog (confirm + optional dismiss). */
@Composable
internal fun OwnerDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String? = null,
    onDismiss: () -> Unit = onConfirm,
    isDestructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.CardSurface,
        title = { Text(text = title, fontWeight = FontWeight.Bold, color = AppColors.TextMain) },
        text = { Text(text = message, color = AppColors.TextSub) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = if (isDestructive) AppColors.Danger else AppTheme.role.accent,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = dismissText?.let {
            { TextButton(onClick = onDismiss) { Text(text = it, color = AppColors.TextSub) } }
        },
    )
}
