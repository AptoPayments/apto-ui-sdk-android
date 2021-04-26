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
    fun `when date is set then continueButton is enabled`() {
        sut.setLocalDate(LocalDate.now())

        assertTrue { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `given user older than 18 when continueClicked then continue Event is fired`() {
        val date = LocalDate.now().minusYears(19)
        sut.setLocalDate(date)

        sut.onContinueClicked()

        val dataPoint = sut.continueClicked.getOrAwaitValue()
        assertEquals(dataPoint.birthdate, date)
    }

    @Test
    fun `given user with 18 yo when continueClicked then continue Event is fired`() {
        val date = LocalDate.now().minusYears(18).minusDays(1)
        sut.setLocalDate(date)

        sut.onContinueClicked()

        val dataPoint = sut.continueClicked.getOrAwaitValue()
        assertEquals(dataPoint.birthdate, date)
    }

    @Test
    fun `given user younger than 18 when continueClicked then error is raised`() {
        val date = LocalDate.now().minusYears(17)
        sut.setLocalDate(date)

        sut.onContinueClicked()

        val error = sut.failure.getOrAwaitValue()
        assertTrue(error is CollectUserBirthdateViewModel.YoungerThanEighteenYO)
    }
}
