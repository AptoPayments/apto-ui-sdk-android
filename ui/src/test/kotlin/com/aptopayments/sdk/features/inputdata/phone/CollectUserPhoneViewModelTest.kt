package com.aptopayments.sdk.features.inputdata.phone

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.test.assertEquals

private const val COUNTRY_CODE = "US"
private const val COUNTRY_CODE_PREFIX = "1"
private const val PHONE_NUMBER = "666777888"

internal class CollectUserPhoneViewModelTest {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private val analyticsManager: AnalyticsManager = mock()

    lateinit var sut: CollectUserPhoneViewModel

    @Before
    fun setUp() {
        sut = CollectUserPhoneViewModel(analyticsManager)
    }

    @Test
    fun `on init tracks correct event`() {
        verify(analyticsManager).track(Event.WorkflowUserPhone)
    }

    @Test
    fun `when valid input then button is enabled`() {
        sut.onCountryChanged(COUNTRY_CODE)
        sut.onPhoneChanged(PHONE_NUMBER, true)

        kotlin.test.assertTrue { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `test for successful phone verification request`() {
        sut.onCountryChanged(COUNTRY_CODE)
        sut.onPhoneChanged(PHONE_NUMBER, true)
        sut.onContinueClicked()

        val event = sut.continuePressed.getOrAwaitValue()

        assertEquals(event.phoneNumber.phoneNumber, PHONE_NUMBER)
        assertEquals(event.phoneNumber.countryCode, COUNTRY_CODE_PREFIX)
    }

    @Test
    fun `when invalid input then button is disabled`() {
        sut.onCountryChanged(COUNTRY_CODE)
        sut.onPhoneChanged(PHONE_NUMBER, false)

        kotlin.test.assertFalse(sut.continueEnabled.getOrAwaitValue())
    }

    @Test
    fun `when nothing is set then continue button is disabled`() {
        kotlin.test.assertFalse { sut.continueEnabled.getOrAwaitValue() }
    }
}
