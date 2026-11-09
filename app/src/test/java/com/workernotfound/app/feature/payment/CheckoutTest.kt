package com.workernotfound.app.feature.payment

import com.workernotfound.app.feature.payment.domain.*
import com.workernotfound.app.feature.payment.data.*
import com.workernotfound.app.feature.job.data.remote.PaymentOrderDto
import org.junit.Assert.*
import org.junit.Test

class CheckoutTest {
    private val checkout = Checkout(5, "order-5", 3, "44080.00".toBigDecimal(), "test_ck_fixture", "nonce-123")
    private val valid = checkout.successUrl + "&orderId=order-5&amount=44080&paymentKey=test_payment"
    @Test fun `only matching origin state order and amount produce an approval receipt`() {
        val receipt = (checkout.parseReturn(valid) as CheckoutReturn.Success).receipt
        assertEquals("order-5", receipt.orderId)
        assertEquals(0, receipt.amount.compareTo(checkout.amount))
        assertNull(checkout.parseReturn(valid.replace("appassets.androidplatform.net", "appassets.androidplatform.net.attacker.invalid")))
        listOf(valid.replace("nonce-123", "other"), valid.replace("amount=44080", "amount=1"),
            valid.replace("orderId=order-5", "orderId=other"), valid + "&amount=44080",
            valid.replace("test_payment", "%3Cscript%3E"), valid.replace("/payment/success", "/payment/other")).forEach { url ->
            assertThrows(IllegalArgumentException::class.java) { checkout.parseReturn(url) }
        }
        assertTrue(checkout.parseReturn(checkout.failUrl) is CheckoutReturn.Failed)
    }
    @Test fun `fresh order and approval receipt must retain original job version and money`() {
        val receipt = CheckoutReceipt("order-5", "44080".toBigDecimal(), "test_payment")
        val order = PaymentOrderDto("order-5", 5, 3, "44080".toBigDecimal(), "KRW", "READY")
        validateCheckoutIdentity(checkout, receipt, order)
        listOf(order.copy(orderId = "other"), order.copy(jobPostId = 8), order.copy(jobVersion = 4),
            order.copy(currency = "USD"), order.copy(amount = "44081".toBigDecimal())).forEach {
            assertThrows(IllegalArgumentException::class.java) { validateCheckoutIdentity(checkout, receipt, it) }
        }
        assertFalse(checkout.toString().contains(checkout.clientKey))
        assertFalse(receipt.toString().contains(receipt.paymentKey))
    }
}
