package com.aptopayments.sdk.features.inputdata.email

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.user.EmailDataPoint
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val PRESET_EMAIL = "jhon@doe.com"

@ExtendWith(InstantExecutorExtension::class)
internal class CollectUserEmailViewModelTest {

    private val analyticsManager: AnalyticsManager = mock()

    @Test
    fun `onViewLoaded tracks correct event`() {
        CollectUserEmailViewModel(null, analyticsManager)

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
