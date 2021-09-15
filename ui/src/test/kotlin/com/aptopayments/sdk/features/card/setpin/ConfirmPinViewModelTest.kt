package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConfirmPinViewModelTest {

    private val aptoPlatform: AptoPlatformProtocol = mock()
    private var analyticsManager: AnalyticsServiceContract = mock()

    private lateinit var sut: ConfirmCardPinViewModel

    @BeforeEach
    fun setUp() {
        sut = ConfirmCardPinViewModel(TestDataProvider.provideCardId(), "1234", analyticsManager, aptoPlatform)
    }

    @Test
    fun `test track is called on view loaded in set pin mode`() {
        verify(analyticsManager).track(Event.ManageCardConfirmPin)
    }
}
