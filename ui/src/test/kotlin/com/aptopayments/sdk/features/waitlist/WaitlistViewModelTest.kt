package com.aptopayments.sdk.features.waitlist

import androidx.lifecycle.Observer
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.features.card.waitlist.WaitlistViewModel
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Spy
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WaitlistViewModelTest : AndroidTest() {

    private lateinit var sut: WaitlistViewModel
    @Spy
    private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()
    @Mock
    private lateinit var mockCard: Card
    @Mock
    private lateinit var cardObserver: Observer<Card>

    @Before
    override fun setUp() {
        super.setUp()
        sut = WaitlistViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on view loaded`() {
        sut.viewLoaded()
        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.Waitlist)
    }
}
