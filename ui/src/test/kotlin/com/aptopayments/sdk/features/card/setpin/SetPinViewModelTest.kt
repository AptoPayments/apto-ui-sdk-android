package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SetPinViewModelTest : AndroidTest() {

    private lateinit var sut: SetCardPinViewModel

    private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()

    @Before
    override fun setUp() {
        super.setUp()
        sut = SetCardPinViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on view loaded in set pin mode`() {
        sut.trackEvent()
        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.ManageCardSetPin)
    }
}
