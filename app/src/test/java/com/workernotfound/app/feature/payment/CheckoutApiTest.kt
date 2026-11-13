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

    /** Server rejects fractional digits (GLOBAL-400-002); order amounts arrive as e.g. 22000.00. */
    @Test fun `confirm sends a whole KRW amount even when the order amount has a scale`(): Unit = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(CheckoutApi::class.java)
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"orderId":"order-16","jobPostId":16,"jobVersion":1,"amount":22000.00,"currency":"KRW","status":"DEPOSITED"}}"""))
            val receipt = com.workernotfound.app.feature.payment.domain.CheckoutReceipt("order-16", "22000.00".toBigDecimal(), "tgen_test_key")
            api.confirm("order-16", confirmRequest(receipt))
            val raw = server.takeRequest().body.readUtf8()
            assertEquals("22000", JsonParser.parseString(raw).asJsonObject["amount"].asBigDecimal.toPlainString())
            assertFalse(raw.contains("22000.00"))
            assertThrows(ArithmeticException::class.java) {
                confirmRequest(com.workernotfound.app.feature.payment.domain.CheckoutReceipt("o", "100.50".toBigDecimal(), "k"))
            }
        } finally { server.shutdown() }
    }
}
