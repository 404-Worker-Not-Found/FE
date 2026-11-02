package com.workernotfound.app.feature.auth.data.remote

import java.math.BigDecimal

class EmailDto(val email: String)
class PhoneDto(val phoneNumber: String)
class EmailCodeDto(val email: String, val verificationCode: String)
class PhoneCodeDto(val phoneNumber: String, val verificationCode: String)
class PasswordResetDto(val email: String, val verificationCode: String, val newPassword: String)
class VerificationDto(val verified: Boolean)
class LocationDto(val address: String, val latitude: BigDecimal, val longitude: BigDecimal)
class AvailableTimeDto(val dayOfWeek: String, val startTime: String, val endTime: String)
class OwnerSignupDto(
    val name: String, val email: String, val password: String, val phoneNumber: String,
    val deviceId: String, val businessRegistrationNumber: String, val storeName: String,
    val businessType: String, val storeLocation: LocationDto,
)
class WorkerSignupDto(
    val name: String, val email: String, val password: String, val phoneNumber: String,
    val deviceId: String, val desiredHourlyWage: Int, val activityRadiusKm: Int,
    val immediatelyAvailable: Boolean, val baseLocation: LocationDto,
    val preferredBusinessTypes: List<String>, val availableTimes: List<AvailableTimeDto>,
)
