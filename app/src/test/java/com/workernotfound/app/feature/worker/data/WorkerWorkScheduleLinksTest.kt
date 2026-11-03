package com.workernotfound.app.feature.worker.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkerWorkScheduleLinksTest {
    @Test fun linkedMockSchedulesResolveToTheSameStoreDateAndTime() = runBlocking {
        val jobs = WorkerJobRepositoryImpl()
        WorkerWorkRepositoryImpl().getWorks().forEach { work ->
            work.jobId?.let { id ->
                val job = jobs.getJobDetail(id)
                assertEquals(work.storeName, job.storeName)
                assertEquals(work.workDate, job.workDate)
                assertEquals(work.timeRange, job.workTimeRange)
                assertEquals(work.address, job.address)
            }
        }
    }
}
