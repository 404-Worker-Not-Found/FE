package com.workernotfound.app.feature.mypage.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.feature.mypage.data.ProfilePhotoReader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Android photo picker requires no broad gallery/storage permission. */
@Composable
fun ProfilePhotoButton(onSelected: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasError by remember { mutableStateOf(false) }
    var isReading by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            isReading = true
            hasError = false
            try {
                if (ProfilePhotoReader.isSupported(context.contentResolver, uri)) onSelected(uri.toString())
                else hasError = true
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                hasError = true
            } finally {
                isReading = false
            }
        }
    }
    Column {
        TextButton(
            onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            enabled = !isReading,
        ) { Text(stringResource(R.string.profile_select_photo), color = AppTheme.role.accentPressed) }
        if (hasError) Text(stringResource(R.string.profile_photo_error), color = AppColors.Danger)
    }
}
