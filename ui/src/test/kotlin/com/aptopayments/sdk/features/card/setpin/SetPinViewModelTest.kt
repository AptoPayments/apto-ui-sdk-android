package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SetPinViewModelTest {

    private lateinit var sut: SetCardPinViewModel

    private var analyticsManager: AnalyticsServiceContract = mock()

    @BeforeEach
    fun setUp() {
        sut = SetCardPinViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on init in set pin mode`() {
        verify(analyticsManager).track(Event.ManageCardSetPin)
    }
}
