package com.aptopayments.sdk.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
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
