package com.workernotfound.app.feature.worker.domain.model

data class WorkerProfile(
    val name: String,
    val email: String,
    val phone: String?,
    val isPushEnabled: Boolean?,
    val isEmailEnabled: Boolean?,
    val isSmsEnabled: Boolean?,
    val profileImageUri: String? = null,
)
