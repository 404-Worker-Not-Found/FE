package com.workernotfound.app.feature.mypage.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.AppColors

/** The caller owns the draft, validation, and save operation. */
@Composable
fun ProfileNameDialog(
    name: String,
    isValid: Boolean,
    isSaving: Boolean,
    hasSaveError: Boolean,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor = AppColors.CardSurface,
        title = { Text(stringResource(R.string.profile_edit_name)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name, onValueChange = onNameChange, singleLine = true,
                    enabled = !isSaving,
                    label = { Text(stringResource(R.string.profile_name)) },
                    isError = !isValid,
                    supportingText = { Text(stringResource(R.string.profile_name_rule)) },
                )
                if (hasSaveError) Text(stringResource(R.string.profile_save_error), color = AppColors.Danger)
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = isValid && !isSaving) {
                Text(stringResource(R.string.profile_save), color = if (isValid && !isSaving) AppTheme.role.accentPressed else AppColors.TextPlaceholder)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(R.string.profile_cancel), color = AppColors.TextSub)
            }
        },
    )
}
