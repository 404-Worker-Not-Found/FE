package com.workernotfound.app.feature.job.data

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.feature.job.data.remote.*
import com.workernotfound.app.feature.job.domain.model.*
import com.workernotfound.app.feature.job.viewmodel.*
import java.time.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PublicationContractTest {
    private val business = PostingBusiness(43, "가게", "서울", "37.5".toBigDecimal(), "127".toBigDecimal())
    private fun draft() = PublicationDraft(business, 7, "공고", "업무", LocalDate.of(2026, 11, 2),
        LocalTime.of(23, 0), LocalTime.of(1, 0), true, 10320, null, 2, "HIGH", LocalDateTime.of(2026, 11, 2, 22, 0))
    @Test fun validatesDeadlineAndOvernightWithoutGuessingExtraWage() {
        val now = LocalDateTime.of(2026, 11, 2, 21, 0)
        val draft = draft()
        assertTrue(draft.isValid(now))
        assertFalse(draft.copy(nextDay = false).isValid(now))
        assertFalse(draft.copy(deadline = now).isValid(now))
        assertFalse(draft.copy(deadline = draft.workDate.atTime(draft.start)).isValid(now))
        assertFalse(draft.copy(categoryId = 0).isValid(now))
        assertFalse(draft.copy(extraWage = -1).isValid(now))
        val dto = JsonParser.parseString(Gson().toJson(draft.toDto())).asJsonObject
        assertEquals(43L, dto["businessId"].asLong)
        assertEquals(7L, dto["categoryId"].asLong)
        assertTrue(dto["isEndTimeNextDay"].asBoolean)
        assertFalse(dto.has("extraWage"))
        assertFalse(dto.has("radius"))
    }
    @Test fun categorySelectionRequiresConfirmedCatalogInsteadOfTypedIds() {
        val tomorrow = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1)
        val category = PostingCategory(7, "카페")
        val state = JobPublicationUiState(business = business, selectedCategory = category, categories = listOf(category), fields = mapOf(
            PublicationField.TITLE to "공고", PublicationField.DESCRIPTION to "업무", PublicationField.DATE to tomorrow.toString(),
            PublicationField.START to "09:00", PublicationField.END to "12:00", PublicationField.BASE_WAGE to "10320",
            PublicationField.COUNT to "2", PublicationField.DEADLINE to tomorrow.atTime(8, 0).toString()))
        assertTrue(state.canCreate)
        assertFalse(state.copy(locationSelected = false).canCreate)
        assertFalse(state.copy(business = business.copy(address = "")).canCreate)
        assertFalse(state.copy(business = business.copy(latitude = "91".toBigDecimal())).canCreate)
        assertFalse(state.copy(categories = emptyList()).canCreate)
        assertFalse(state.copy(selectedCategory = null).canCreate)
        assertFalse(state.copy(selectedCategory = PostingCategory(99, "미확인 업종")).canCreate)
    }
    @Test fun publicationAppearsInManagementAndApplicantBoardWithUniqueIds() {
        val store = com.workernotfound.app.feature.owner.data.OwnerMockStore()
        val first = store.publish(draft())
        val second = store.publish(draft().copy(title = "다음 공고"))
        assertNotEquals(first, second)
        val posting = store.postings(System.currentTimeMillis()).first { it.id == first.toString() }
        assertEquals("공고", posting.workSummary)
        assertEquals(com.workernotfound.app.feature.owner.domain.model.PostingStatus.RECRUITING, posting.status)
        assertTrue(store.applicantBoard(first.toString(), System.currentTimeMillis()).applicants.isEmpty())
    }

    @Test fun invalidInputsExplainWhyPublicationCannotContinue() {
        val base = JobPublicationUiState(fields = mapOf(PublicationField.BASE_WAGE to "100"))
        assertEquals(PublicationValidationIssue.WAGE, base.validationIssue)
        assertEquals(PublicationValidationIssue.COUNT, base.copy(fields = mapOf(PublicationField.COUNT to "11")).validationIssue)
        val overnight = base.copy(fields = mapOf(PublicationField.START to "23:00", PublicationField.END to "01:00"))
        assertEquals(PublicationValidationIssue.SCHEDULE, overnight.validationIssue)
        assertNull(overnight.copy(nextDay = true).validationIssue)
        assertEquals(PublicationValidationIssue.DEADLINE, base.copy(fields = mapOf(
            PublicationField.DEADLINE to LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(1).toString()
        )).validationIssue)
    }

    @Test fun paymentAndPublicationRemainSeparateStates() {
        val pending = PublicationPayment(5, "PAYMENT_PENDING", "CREATED", "order", 50000.toBigDecimal(), "KRW", "DEPOSITED", false)
        assertFalse(pending.isPublished)
        assertTrue(pending.copy(jobStatus = "OPEN").isPublished)
        val job = JobPaymentDto(5, "PAYMENT_PENDING", "CREATED", "order", 4, 50000.toBigDecimal(), "KRW", null)
        val order = PaymentOrderDto("order", 5, 4, "50000.00".toBigDecimal(), "KRW", "READY")
        validateOrder(job, order)
        listOf(order.copy(jobPostId = 6), order.copy(jobVersion = 5), order.copy(amount = 49999.toBigDecimal()),
            order.copy(currency = "USD"), order.copy(orderId = "other")).forEach {
            try { validateOrder(job, it); fail("Mismatched order accepted") } catch (_: ApiException) { }
        }
    }
    @Test fun createAndPaymentQueriesUsePublicContractsAndRealReturnedIds() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(JobPublicationApiService::class.java)
            server.enqueue(MockResponse().setBody("""{"success":true,"data":117}"""))
            val id = api.create(draft().toDto()).requireData()
            assertEquals(117L, id)
            assertEquals("/api/jobs", server.takeRequest().path)
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"jobPostId":117,"jobStatus":"PAYMENT_PENDING","orderCreationStatus":"PENDING","paymentOrderId":null,"paymentJobVersion":0,"amount":50000,"currency":"KRW"}}"""))
            val payment = api.payment(id).requireData()
            assertNull(payment.paymentOrderId)
            assertEquals("PENDING", payment.orderCreationStatus)
            assertEquals("/api/jobs/117/payment-order", server.takeRequest().path)
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"orderId":"real-order","jobPostId":117,"jobVersion":0,"amount":50000,"currency":"KRW","status":"READY"}}"""))
            assertEquals("READY", api.order("real-order").requireData().status)
            assertEquals("/api/payments/orders/real-order", server.takeRequest().path)
        } finally { server.shutdown() }
    }
}
