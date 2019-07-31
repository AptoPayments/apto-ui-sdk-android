package com.aptopayments.sdk.features.issuecard

import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import org.junit.Before
import org.junit.Test
import org.mockito.Spy
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IssueCardViewModelTest : UnitTest() {

    private lateinit var sut: IssueCardViewModel
    @Spy private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()

    @Before
    override fun setUp() {
        super.setUp()
        sut = IssueCardViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on view loaded`() {
        sut.viewLoaded()
        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.IssueCard)
    }
}
