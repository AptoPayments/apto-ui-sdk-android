package com.aptopayments.sdk.features.card.fundingsources

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FundingSourcesViewModelTest {
    private lateinit var sut: FundingSourcesViewModel

    private var analyticsManager: AnalyticsServiceContract = mock()

    @BeforeEach
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
