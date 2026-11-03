package com.workernotfound.app.feature.worker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class WorkerProfileRepositoryTest {
    @Test fun nameAndNotificationChangesRemainAvailableToNewObservers() {
        val repository = WorkerProfileRepositoryImpl()
        repository.updateProfile(repository.profile.value.copy(name = "민주", isPushEnabled = false))
        val observed = repository.profile.value
        assertEquals("민주", observed.name)
        assertFalse(observed.isPushEnabled)
        assertEquals("leegeonun@example.com", observed.email)
    }

    @Test fun invalidNameDoesNotMutateExistingProfile() {
        val repository = WorkerProfileRepositoryImpl()
        val original = repository.profile.value
        runCatching { repository.updateProfile(original.copy(name = "1")) }
        assertEquals(original, repository.profile.value)
    }
}
