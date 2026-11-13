package com.workernotfound.app.feature.payment.data

import com.workernotfound.app.BuildConfig
import com.workernotfound.app.core.network.*
import com.workernotfound.app.feature.job.data.remote.*
import com.workernotfound.app.feature.job.data.validateOrder
import com.workernotfound.app.feature.payment.domain.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import java.math.BigDecimal
import java.util.UUID
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface CheckoutApi {
    @POST("api/payments/orders/{orderId}/confirm")
    suspend fun confirm(@Path("orderId") orderId: String, @Body request: ConfirmCheckoutDto): ApiResponse<PaymentOrderDto>
}
/**
 * payment-service ConfirmOrderRequest requires a whole KRW amount (@Digits fraction = 0), while order
 * responses carry a scale (e.g. 22000.00). Send it without the fraction; a real fraction is rejected here.
 */
internal fun confirmRequest(receipt: CheckoutReceipt) =
    ConfirmCheckoutDto(receipt.paymentKey, receipt.amount.setScale(0, java.math.RoundingMode.UNNECESSARY))

class ConfirmCheckoutDto(val paymentKey: String, val amount: BigDecimal) {
    override fun toString() = "ConfirmCheckoutDto()"
}
class CheckoutRepositoryImpl @Inject constructor(private val jobs: JobPublicationApiService,
    private val api: CheckoutApi, private val backend: BackendAccess) : CheckoutRepository {
    override suspend fun prepare(jobId: Long): Checkout {
        check(BuildConfig.TOSS_CLIENT_KEY.startsWith("test_ck_"))
        backend.requireSession()
        val (job, order) = currentOrder(jobId)
        check(job.jobStatus == "PAYMENT_PENDING" && order.status == "READY")
        check(order.amount >= BigDecimal(100) && order.amount.stripTrailingZeros().scale() <= 0 && order.amount <= BigDecimal("9007199254740991"))
        return Checkout(jobId, order.orderId, order.jobVersion, order.amount, BuildConfig.TOSS_CLIENT_KEY, UUID.randomUUID().toString())
    }
    override suspend fun confirm(checkout: Checkout, receipt: CheckoutReceipt) {
        backend.requireSession()
        val (_, order) = currentOrder(checkout.jobId)
        validateCheckoutIdentity(checkout, receipt, order)
        if (order.status == "DEPOSITED") return
        check(order.status in setOf("READY", "CONFIRMING"))
        val accepted = api.confirm(order.orderId, confirmRequest(receipt)).requireData()
        validateCheckoutIdentity(checkout, receipt, accepted)
        check(accepted.status in setOf("DEPOSITED", "CONFIRMING"))
    }
    private suspend fun currentOrder(jobId: Long): Pair<JobPaymentDto, PaymentOrderDto> {
        val job = jobs.payment(jobId).requireData()
        check(job.jobPostId == jobId && job.orderCreationStatus == "CREATED" && job.latestChange?.status != "PENDING")
        val order = jobs.order(requireNotNull(job.paymentOrderId)).requireData()
        validateOrder(job, order)
        return job to order
    }
}
internal fun validateCheckoutIdentity(checkout: Checkout, receipt: CheckoutReceipt, order: PaymentOrderDto) {
    require(receipt.orderId == checkout.orderId && receipt.amount.compareTo(checkout.amount) == 0 &&
        order.orderId == checkout.orderId && order.jobPostId == checkout.jobId && order.jobVersion == checkout.version &&
        order.currency == "KRW" && order.amount.compareTo(checkout.amount) == 0)
    require(Regex("[A-Za-z0-9_-]{1,200}").matches(receipt.paymentKey))
}
@Module @InstallIn(SingletonComponent::class)
object CheckoutModule {
    @Provides fun api(client: OkHttpClient): CheckoutApi = Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL)
        .client(client).addConverterFactory(GsonConverterFactory.create()).build().create(CheckoutApi::class.java)
    @Provides fun repository(implementation: CheckoutRepositoryImpl): CheckoutRepository = implementation
}
