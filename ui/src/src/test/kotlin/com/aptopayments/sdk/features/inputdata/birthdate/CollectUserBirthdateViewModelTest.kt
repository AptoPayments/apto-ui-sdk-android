package com.aptopayments.sdk.features.inputdata.birthdate

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.threeten.bp.LocalDate

class CollectUserBirthdateViewModelTest : UnitTest() {
    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var analyticsManager: AnalyticsManager

    private lateinit var sut: CollectUserBirthdateViewModel

    @Before
    fun setUp() {
        sut = CollectUserBirthdateViewModel(analyticsManager)
    }

    @Test
    fun `when viewLoaded then correct tracking is done`() {
        sut.viewLoaded()

        verify(analyticsManager).track(Event.WorkflowUserBirthdate)
    }

    @Test
    fun `when created continue is disabled`() {
        assertFalse(sut.continueEnabled.getOrAwaitValue())
    }

    @Test
    fun `when date is set then continueButton is enabled`() {
        sut.setLocalDate(LocalDate.now())

        kotlin.test.assertTrue { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `when continueClicked and verified then event is fired`() {
        val dateNow = LocalDate.now()
        sut.setLocalDate(dateNow)
        sut.onContinueClicked()

        val dataPoint = sut.continueClicked.getOrAwaitValue()
        assertEquals(dataPoint.birthdate, dateNow)
    }
}
