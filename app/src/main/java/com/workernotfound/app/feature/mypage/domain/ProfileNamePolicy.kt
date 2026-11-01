package com.workernotfound.app.feature.mypage.domain

/** UI specification 4-1: Korean/English, 2–20 characters. */
object ProfileNamePolicy {
    fun isValid(name: String): Boolean = name.matches(Regex("[가-힣a-zA-Z]{2,20}"))
}
