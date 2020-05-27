package com.aptopayments.sdk.features.card.fundingsources

import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import org.junit.Before
import org.junit.Test
import org.mockito.Spy
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FundingSourcesViewModelTest : AndroidTest() {
    private lateinit var sut: FundingSourcesViewModel

    @Spy
    private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()

    @Before
    override fun setUp() {
        super.setUp()
        sut = FundingSourcesViewModel(analyticsManager)
    }

    @Test
    fun `view loaded track analytics`() {
        // When
        sut.viewLoaded()

        // Then
        assertTrue(analyticsManager.trackCalled)
        assertEquals(Event.ManageCardFundingSourceSelector, analyticsManager.lastEvent)
    }
}
