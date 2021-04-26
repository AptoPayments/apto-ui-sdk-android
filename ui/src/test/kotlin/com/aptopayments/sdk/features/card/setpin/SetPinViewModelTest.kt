package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test

class SetPinViewModelTest {

    private lateinit var sut: SetCardPinViewModel

    private var analyticsManager: AnalyticsServiceContract = mock()

    @Before
    fun setUp() {
        sut = SetCardPinViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on init in set pin mode`() {
        verify(analyticsManager).track(Event.ManageCardSetPin)
    }
}
