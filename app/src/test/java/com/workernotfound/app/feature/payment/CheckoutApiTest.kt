package com.workernotfound.app.feature.payment

import com.workernotfound.app.feature.payment.data.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.JsonParser

class CheckoutApiTest {
    @Test fun `approval uses public backend endpoint and contains no merchant secret`() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(CheckoutApi::class.java)
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"orderId":"order-5","jobPostId":5,"jobVersion":3,"amount":44080,"currency":"KRW","status":"DEPOSITED"}}"""))
            assertEquals("DEPOSITED", api.confirm("order-5", ConfirmCheckoutDto("test_payment", "44080".toBigDecimal())).requireData().status)
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/api/payments/orders/order-5/confirm", request.path)
            val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
            assertEquals(setOf("paymentKey", "amount"), body.keySet())
            assertEquals(44080, body["amount"].asInt)
            assertNull(request.getHeader("Authorization"))
        } finally { server.shutdown() }
    }
}
