package com.workernotfound.app.feature.mypage.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileNamePolicyTest {
    @Test fun acceptsKoreanEnglishAndLengthBoundaries() {
        listOf("민주", "Minju", "김Minju", "가".repeat(20)).forEach {
            assertTrue(it, ProfileNamePolicy.isValid(it))
        }
    }

    @Test fun rejectsInvalidCharactersAndLengths() {
        listOf("", "김", "가".repeat(21), "민주1", "민 주", "민주!", " minju", "minju ", "ㄱㄴ").forEach {
            assertFalse(it, ProfileNamePolicy.isValid(it))
        }
    }
}
