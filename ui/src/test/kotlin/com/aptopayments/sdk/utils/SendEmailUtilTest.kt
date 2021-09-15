package com.aptopayments.sdk.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.Test

class SendEmailUtilTest {
    private val activity: Activity = mock()
    private val sut = SendEmailUtil()

    @Test
    fun `given no activity found when execute then Failure is returned`() {
        whenever(activity.startActivity(any())).thenThrow(ActivityNotFoundException::class.java)

        val result = sut.execute(activity)

        result.shouldBeLeftAndInstanceOf(NoEmailClientConfiguredFailure::class.java)
    }

    @Test
    fun `given activity found when execute then Unit is returned`() {
        val result = sut.execute(activity)

        result.shouldBeRightAndEqualTo(Unit)
    }

    @Test
    fun `given activity found when execute then activity has been started`() {
        sut.execute(activity)

        verify(activity).startActivity(any())
    }
}
