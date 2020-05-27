package com.aptopayments.sdk.features.inputdata.phone

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import kotlin.test.assertEquals

private const val COUNTRY_CODE = "US"
private const val COUNTRY_CODE_PREFIX = "1"
private const val PHONE_NUMBER = "666777888"

internal class CollectUserPhoneViewModelTest : UnitTest() {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    lateinit var analyticsManager: AnalyticsManager

    lateinit var sut: CollectUserPhoneViewModel

    @Before
    fun setUp() {
        sut = CollectUserPhoneViewModel(analyticsManager)
    }

    @Test
    fun `onViewLoaded tracks correct event`() {
        sut.viewLoaded()

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
