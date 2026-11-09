package com.workernotfound.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.NAVER_MAP_CLIENT_ID.isNotBlank()) {
            val sdk = com.naver.maps.map.NaverMapSdk.getInstance(this)
            sdk.onAuthFailedListener = com.naver.maps.map.NaverMapSdk.OnAuthFailedListener {
                com.workernotfound.app.core.maps.MapAvailability.authFailed.value = true
            }
            sdk.client = com.naver.maps.map.NaverMapSdk.NcpKeyClient(BuildConfig.NAVER_MAP_CLIENT_ID)
        }
    }
}
