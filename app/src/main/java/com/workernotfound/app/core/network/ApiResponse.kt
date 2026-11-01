package com.workernotfound.app.core.network

/** Actual server envelope, including auth/job whose Swagger success schema omits it. */
data class ApiResponse<T>(
    val success: Boolean,
    val code: String?,
    val data: T?,
) {
    fun requireData(): T {
        if (!success) throw ApiException(code)
        return data ?: throw ApiException("EMPTY_RESPONSE")
    }
}

class ApiException(val code: String?) : Exception("서버 요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.")
