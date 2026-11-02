package com.workernotfound.app.feature.account.data

import com.workernotfound.app.core.network.ApiException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ContactCodeFailureTest {
    @Test fun `only confirmed pre-persistence invalid code permits a new command`() {
        assertTrue(isRejectedContactCode(ApiException("AUTH-400-005")))
        assertTrue(isRejectedContactCode(http(400, """{"code":"AUTH-400-005"}""")))
        assertFalse(isRejectedContactCode(IOException()))
        assertFalse(isRejectedContactCode(ApiException("OTHER")))
        assertFalse(isRejectedContactCode(http(400, "broken")))
        assertFalse(isRejectedContactCode(http(409, """{"code":"AUTH-400-005"}""")))
        assertFalse(isRejectedContactCode(http(500, """{"code":"AUTH-400-005"}""")))
    }
    private fun http(status: Int, body: String) = HttpException(Response.error<Any>(status, body.toResponseBody()))
}
