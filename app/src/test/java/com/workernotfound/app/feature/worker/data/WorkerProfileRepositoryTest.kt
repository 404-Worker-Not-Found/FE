package com.workernotfound.app.feature.worker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class WorkerProfileRepositoryTest {
    @Test fun nameAndNotificationChangesRemainAvailableToNewObservers() {
        val repository = DemoWorkerProfileStore()
        repository.update(repository.profile.copy(name = "민주", isPushEnabled = false))
        val observed = repository.profile
        assertEquals("민주", observed.name)
        assertEquals(false, observed.isPushEnabled)
        assertEquals("leegeonun@example.com", observed.email)
    }

    @Test fun invalidNameDoesNotMutateExistingProfile() {
        val repository = DemoWorkerProfileStore()
        val original = repository.profile
        runCatching { repository.update(original.copy(name = "1")) }
        assertEquals(original, repository.profile)
    }
}
