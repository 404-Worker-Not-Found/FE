package com.workernotfound.app.feature.mypage.ui

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.feature.mypage.data.ProfileCameraFiles
import com.workernotfound.app.feature.mypage.data.ProfilePhotoReader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Pick gallery content or delegate capture to a camera app without CAMERA/storage permissions. */
@Composable
fun ProfilePhotoButton(onSelected: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var messageRes by rememberSaveable { mutableStateOf<Int?>(null) }
    var isReading by remember { mutableStateOf(false) }
    var pendingCapture by rememberSaveable { mutableStateOf<String?>(null) }
    var isGalleryOpen by rememberSaveable { mutableStateOf(false) }

    suspend fun readPhoto(uri: Uri, capture: String? = null) {
        isReading = true
        var accepted = false
        try {
            if (ProfilePhotoReader.isSupported(context.contentResolver, uri)) {
                onSelected(uri.toString())
                accepted = true
            } else messageRes = R.string.profile_photo_error
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            messageRes = R.string.profile_photo_error
        } finally {
            if (!accepted && capture != null) ProfileCameraFiles.discard(context, capture)
            isReading = false
        }
    }

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        isGalleryOpen = false
        if (uri != null) scope.launch { readPhoto(uri) }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val capture = pendingCapture
        pendingCapture = null
        scope.launch {
            if (success && capture != null) readPhoto(ProfileCameraFiles.uri(context, capture), capture)
            else {
                if (capture != null) ProfileCameraFiles.discard(context, capture)
                messageRes = if (success) R.string.profile_camera_failed else R.string.profile_camera_cancelled
            }
        }
    }
    val enabled = !isReading && pendingCapture == null && !isGalleryOpen
    Column {
        TextButton(
            onClick = {
                messageRes = null
                isGalleryOpen = true
                try {
                    gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } catch (error: Exception) {
                    isGalleryOpen = false
                    messageRes = R.string.profile_gallery_unavailable
                }
            }, enabled = enabled,
        ) { Text(stringResource(R.string.profile_select_photo), color = AppTheme.role.accentPressed) }
        TextButton(
            onClick = {
                messageRes = null
                isReading = true
                scope.launch {
                    try {
                        val capture = ProfileCameraFiles.create(context)
                        pendingCapture = capture
                        camera.launch(ProfileCameraFiles.uri(context, capture))
                    } catch (error: Exception) {
                        if (error is CancellationException) throw error
                        pendingCapture?.let { ProfileCameraFiles.discard(context, it) }
                        pendingCapture = null
                        messageRes = if (error is ActivityNotFoundException) R.string.profile_camera_unavailable
                            else R.string.profile_camera_failed
                    } finally {
                        isReading = false
                    }
                }
            }, enabled = enabled,
        ) { Text(stringResource(R.string.profile_take_photo), color = AppTheme.role.accentPressed) }
        messageRes?.let {
            Text(
                stringResource(it),
                color = if (it == R.string.profile_camera_cancelled) AppColors.TextSub else AppColors.Danger,
            )
        }
    }
}
