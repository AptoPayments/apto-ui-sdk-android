package com.aptopayments.sdk.features.inputdata.email

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.user.EmailDataPoint
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.verify
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val PRESET_EMAIL = "jhon@doe.com"

internal class CollectUserEmailViewModelTest : UnitTest() {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    lateinit var analyticsManager: AnalyticsManager

    @Test
    fun `onViewLoaded tracks correct event`() {
        val sut = CollectUserEmailViewModel(null, analyticsManager)

        sut.viewLoaded()

        verify(analyticsManager).track(Event.WorkflowUserInputEmail)
    }

    @Test
    fun `when nothing is set then continue button is disabled`() {
        val sut = CollectUserEmailViewModel(null, analyticsManager)

        assertFalse { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `when invalid email is set then continue button is disabled`() {
        val sut = CollectUserEmailViewModel(null, analyticsManager)

        sut.email.value = "a@"

        assertFalse { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `when valid email is set then continue button is enabled`() {
        val sut = CollectUserEmailViewModel(null, analyticsManager)

        sut.email.value = "neo@matrix.com"

        assertTrue { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `when datapoint is set in constructor then continue button is enabled`() {
        val datapoint = EmailDataPoint(PRESET_EMAIL)

        val sut = CollectUserEmailViewModel(datapoint, analyticsManager)

        assertTrue { sut.continueEnabled.getOrAwaitValue() }
        assertEquals(PRESET_EMAIL, sut.email.getOrAwaitValue())
    }
}
