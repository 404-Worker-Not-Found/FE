package com.workernotfound.app.core.maps

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import android.os.Bundle
import android.content.ComponentCallbacks
import android.content.res.Configuration
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.workernotfound.app.BuildConfig
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import kotlinx.coroutines.flow.MutableStateFlow

object MapAvailability { val authFailed = MutableStateFlow(false) }
data class MapPoint(val id: String, val latitude: Double, val longitude: Double, val title: String) {
    val valid get() = latitude.isFinite() && longitude.isFinite() && latitude in -90.0..90.0 && longitude in -180.0..180.0
}

@Composable
fun AppMap(points: List<MapPoint>, modifier: Modifier = Modifier, onMarker: (String) -> Unit = {},
    onMapPoint: ((Double, Double) -> Unit)? = null) {
    if (BuildConfig.NAVER_MAP_CLIENT_ID.isBlank()) {
        Box(modifier.padding(AppDimens.screenPadding)) { Text(stringResource(R.string.map_not_configured), color = AppColors.TextSub) }
        return
    }
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember(context) { MapView(context) }
    val savedMap = rememberSaveable { Bundle() }
    var map by remember { mutableStateOf<NaverMap?>(null) }
    val markerClick by rememberUpdatedState(onMarker)
    val mapClick by rememberUpdatedState(onMapPoint)
    val authFailed by MapAvailability.authFailed.collectAsStateWithLifecycle()
    DisposableEffect(mapView, lifecycle) {
        mapView.onCreate(savedMap)
        var started = false
        var resumed = false
        var disposed = false
        fun pause() { if (resumed) { mapView.onSaveInstanceState(savedMap); mapView.onPause(); resumed = false } }
        fun stop() { pause(); if (started) { mapView.onStop(); started = false } }
        val observer = LifecycleEventObserver { _, event -> when (event) {
            Lifecycle.Event.ON_START -> if (!started) { mapView.onStart(); started = true }
            Lifecycle.Event.ON_RESUME -> if (!resumed) { mapView.onResume(); resumed = true }
            Lifecycle.Event.ON_PAUSE -> pause()
            Lifecycle.Event.ON_STOP -> stop()
            else -> Unit
        } }
        lifecycle.addObserver(observer)
        val memory = object : ComponentCallbacks {
            override fun onConfigurationChanged(configuration: Configuration) = Unit
            override fun onLowMemory() { mapView.onLowMemory() }
        }
        context.registerComponentCallbacks(memory)
        mapView.getMapAsync { ready ->
            if (!disposed) {
                map = ready
                ready.setOnMapClickListener { _, coordinate -> mapClick?.invoke(coordinate.latitude, coordinate.longitude) }
            }
        }
        onDispose { disposed = true; lifecycle.removeObserver(observer); context.unregisterComponentCallbacks(memory); map = null; stop(); mapView.onDestroy() }
    }
    DisposableEffect(map, points) {
        val ready = map
        val markers = if (ready == null) emptyList() else points.filter { it.valid }.map { point ->
            Marker().apply {
                position = LatLng(point.latitude, point.longitude)
                captionText = point.title
                setOnClickListener { markerClick(point.id); true }
                this.map = ready
            }
        }
        points.firstOrNull { it.valid }?.let { if (ready != null) ready.moveCamera(CameraUpdate.scrollTo(LatLng(it.latitude, it.longitude))) }
        onDispose { markers.forEach { it.map = null } }
    }
    Box(modifier) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
        if (authFailed) Text(stringResource(R.string.map_auth_failed), color = AppColors.Danger, modifier = Modifier.padding(AppDimens.screenPadding))
    }
}
