package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfirmPinViewModelTest : AndroidTest() {

    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()

    private lateinit var sut: ConfirmCardPinViewModel

    @Before
    override fun setUp() {
        super.setUp()
        sut = ConfirmCardPinViewModel(TestDataProvider.provideCardId(), "1234", analyticsManager, aptoPlatform)
    }

    @Test
    fun `test track is called on view loaded in set pin mode`() {
        sut.trackEvent()
        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.ManageCardConfirmPin)
    }
}
