package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test

class ConfirmPinViewModelTest {

    private val aptoPlatform: AptoPlatformProtocol = mock()
    private var analyticsManager: AnalyticsServiceContract = mock()

    private lateinit var sut: ConfirmCardPinViewModel

    @Before
    fun setUp() {
        sut = ConfirmCardPinViewModel(TestDataProvider.provideCardId(), "1234", analyticsManager, aptoPlatform)
    }

    @Test
    fun `test track is called on view loaded in set pin mode`() {
        verify(analyticsManager).track(Event.ManageCardConfirmPin)
    }
}
