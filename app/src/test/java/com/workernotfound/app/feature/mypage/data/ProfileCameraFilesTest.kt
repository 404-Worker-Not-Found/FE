package com.workernotfound.app.feature.mypage.data

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ProfileCameraFilesTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun deletesOnlyTheRequestedAppCameraTemporaryFile() {
        val cache = temporary.newFolder("profile_camera")
        val cancelled = cache.resolve("capture_cancelled.jpg").apply { writeText("camera output") }
        val selected = cache.resolve("capture_selected.jpg").apply { writeText("profile preview") }
        assertTrue(ProfileCameraFiles.discard(cache, cancelled.name))
        assertFalse(cancelled.exists())
        assertTrue(selected.exists())
    }

    @Test fun refusesTraversalGalleryNamesAndSymlinkTargets() {
        val cache = temporary.newFolder("profile_camera")
        val original = temporary.newFile("capture_original.jpg").apply { writeText("user original") }
        val gallery = cache.resolve("gallery.jpg").apply { writeText("gallery original") }
        val link = cache.resolve("capture_link.jpg")
        Files.createSymbolicLink(link.toPath(), original.toPath())
        assertFalse(ProfileCameraFiles.discard(cache, "../capture_original.jpg"))
        assertFalse(ProfileCameraFiles.discard(cache, gallery.name))
        assertFalse(ProfileCameraFiles.discard(cache, link.name))
        assertTrue(original.exists())
        assertTrue(gallery.exists())
    }
}
