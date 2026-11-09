package com.workernotfound.app.feature.job.data

import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.feature.job.data.remote.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CategoryCatalogContractTest {
    @Test fun `public hierarchical catalog preserves server IDs and parent context`() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(JobPublicationApiService::class.java)
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"categories":[{"id":17,"name":"외식/음료","subCategories":[{"id":43,"name":"카페/음료"}]},{"id":25,"name":"교육","subCategories":[]}]}}"""))
            val choices = api.categories().requireData().toPostingCategories()
            assertEquals("/api/jobs/categories", server.takeRequest().path)
            assertEquals(listOf(17L, 43L, 25L), choices.map { it.id })
            assertEquals("외식/음료 / 카페/음료", choices[1].name)
            assertEquals("카페/음료", choices[1].key)
            assertEquals("교육", choices[2].key)
        } finally { server.shutdown() }
    }
    @Test fun `empty catalog stays empty and invalid IDs cannot enable registration`() {
        assertTrue(JobCategoryListDto(emptyList()).toPostingCategories().isEmpty())
        val parent = JobCategoryDto(17, "외식/음료", listOf(JobSubCategoryDto(43,"카페/음료")))
        listOf(parent.copy(id = 0), parent.copy(name = ""), parent.copy(subCategories = listOf(JobSubCategoryDto(17,"중복"))),
            parent.copy(subCategories = listOf(JobSubCategoryDto(-1,"하위")))).forEach {
            assertThrows(ApiException::class.java) { JobCategoryListDto(listOf(it)).toPostingCategories() }
        }
    }
}
