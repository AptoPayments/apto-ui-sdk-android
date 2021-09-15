package com.aptopayments.sdk.features.card.fundingsources

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
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
