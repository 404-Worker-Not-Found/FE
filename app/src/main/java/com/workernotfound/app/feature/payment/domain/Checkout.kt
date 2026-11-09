package com.workernotfound.app.feature.payment.domain

import java.math.BigDecimal
import java.net.URI
import java.net.URLDecoder

const val CHECKOUT_ORIGIN = "https://appassets.androidplatform.net"
data class Checkout(val jobId: Long, val orderId: String, val version: Long, val amount: BigDecimal,
    val clientKey: String, val nonce: String) {
    val successUrl get() = "$CHECKOUT_ORIGIN/payment/success?state=$nonce"
    val failUrl get() = "$CHECKOUT_ORIGIN/payment/fail?state=$nonce"
    override fun toString() = "Checkout(jobId=$jobId)"
}
class CheckoutReceipt(val orderId: String, val amount: BigDecimal, val paymentKey: String) {
    override fun toString() = "CheckoutReceipt()"
}
sealed interface CheckoutReturn {
    class Success(val receipt: CheckoutReceipt) : CheckoutReturn
    data object Failed : CheckoutReturn
}
/** Exact origin/path/state, single-valued parameters and exact server amount; never logs a callback URL. */
fun Checkout.parseReturn(url: String): CheckoutReturn? {
    val uri = URI(url)
    if (uri.scheme != "https" || uri.host != "appassets.androidplatform.net" || uri.port != -1 || uri.rawUserInfo != null) return null
    require(uri.path in setOf("/payment/success", "/payment/fail"))
    val pairs = uri.rawQuery.orEmpty().split('&').filter { it.isNotBlank() }.map {
        val split = it.split('=', limit = 2)
        URLDecoder.decode(split[0], "UTF-8") to URLDecoder.decode(split.getOrElse(1) { "" }, "UTF-8")
    }
    require(pairs.map { it.first }.distinct().size == pairs.size)
    val parameters = pairs.toMap()
    require(parameters["state"] == nonce)
    if (uri.path == "/payment/fail") return CheckoutReturn.Failed
    val returnedAmount = parameters["amount"]?.toBigDecimalOrNull()
    require(parameters["orderId"] == orderId && returnedAmount != null && returnedAmount.compareTo(amount) == 0)
    val key = parameters["paymentKey"].orEmpty()
    require(Regex("[A-Za-z0-9_-]{1,200}").matches(key))
    return CheckoutReturn.Success(CheckoutReceipt(orderId, amount, key))
}
interface CheckoutRepository {
    suspend fun prepare(jobId: Long): Checkout
    suspend fun confirm(checkout: Checkout, receipt: CheckoutReceipt)
}
