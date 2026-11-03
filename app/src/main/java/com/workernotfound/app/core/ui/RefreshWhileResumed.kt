package com.workernotfound.app.core.ui

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay

/** REST freshness while this destination is visible; never starts background polling. */
@Composable
fun RefreshWhileResumed(enabled: Boolean, onRefresh: () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val refresh by rememberUpdatedState(onRefresh)
    LaunchedEffect(lifecycle, enabled) {
        if (enabled) lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) { refresh(); delay(15_000) }
        }
    }
}
