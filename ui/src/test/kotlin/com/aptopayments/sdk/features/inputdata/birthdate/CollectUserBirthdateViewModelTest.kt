package com.aptopayments.sdk.features.inputdata.birthdate

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.threeten.bp.LocalDate
import kotlin.test.assertTrue

class CollectUserBirthdateViewModelTest {
    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private val analyticsManager: AnalyticsManager = mock()

    private lateinit var sut: CollectUserBirthdateViewModel

    @Before
    fun setUp() {
        sut = CollectUserBirthdateViewModel(analyticsManager)
    }

    @Test
    fun `when init then correct tracking is done`() {
        verify(analyticsManager).track(Event.WorkflowUserBirthdate)
    }

    @Test
    fun `when created continue is disabled`() {
        assertFalse(sut.continueEnabled.getOrAwaitValue())
    }

    @Test
    fun `when set a DOB older than 18 years then continue is enabled`() {
        val date = LocalDate.now().minusYears(20)

        sut.setLocalDate(date)

        assertTrue { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `given a DOB older than 18 years when onContinueClicked then event is fired`() {
        val date = LocalDate.now().minusYears(20)
        sut.setLocalDate(date)

        sut.onContinueClicked()

        val dataPoint = sut.continueClicked.getOrAwaitValue()
        assertEquals(dataPoint.birthdate, date)
    }

    @Test
    fun `when set a DOB younger than 18 years then continue is not enabled`() {
        val date = LocalDate.now().minusYears(18).plusDays(1)

        sut.setLocalDate(date)

        assertFalse(sut.continueEnabled.getOrAwaitValue())
    }

    @Test
    fun `when set a DOB younger than 18 years then error is fired`() {
        val date = LocalDate.now().minusYears(18).plusDays(1)

        sut.setLocalDate(date)

        val failure = sut.failure.getOrAwaitValue()
        assertTrue(failure is CollectUserBirthdateViewModel.YoungerThanMinAgeFailure)
    }

    @Test
    fun `when set a DOB older than 120 years then continue is not enabled`() {
        val date = LocalDate.now().minusYears(121)

        sut.setLocalDate(date)

        assertFalse(sut.continueEnabled.getOrAwaitValue())
    }

    @Test
    fun `when set a DOB older than 120 years then error is fired`() {
        val date = LocalDate.now().minusYears(121)

        sut.setLocalDate(date)

        val failure = sut.failure.getOrAwaitValue()
        assertTrue(failure is CollectUserBirthdateViewModel.OlderThanMaxAgeFailure)
    }
}
