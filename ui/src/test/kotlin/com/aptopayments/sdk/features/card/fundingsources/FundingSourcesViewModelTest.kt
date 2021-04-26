package com.aptopayments.sdk.features.card.fundingsources

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test

class FundingSourcesViewModelTest {
    private lateinit var sut: FundingSourcesViewModel

    private var analyticsManager: AnalyticsServiceContract = mock()

    @Before
    fun setUp() {
        sut = FundingSourcesViewModel(analyticsManager)
    }

    @Test
    fun `view loaded track analytics`() {
        // When
        sut.viewLoaded()

        // Then
        verify(analyticsManager).track(Event.ManageCardFundingSourceSelector)
    }
}
