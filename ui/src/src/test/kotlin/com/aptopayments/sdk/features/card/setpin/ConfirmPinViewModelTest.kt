package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import org.junit.Before
import org.junit.Test
import org.mockito.Spy
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfirmPinViewModelTest : AndroidTest() {

    private lateinit var sut: ConfirmPinViewModel
    @Spy
    private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()

    @Before
    override fun setUp() {
        super.setUp()
        sut = ConfirmPinViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on view loaded in set pin mode`() {
        sut.viewLoaded()
        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.ManageCardConfirmPin)
    }
}
