package com.aptopayments.sdk.features.oauth.verify

import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import org.junit.Before
import org.junit.Test
import org.mockito.Spy
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OAuthVerifyViewModelTest : UnitTest() {

    private lateinit var sut: OAuthVerifyViewModel

    @Spy private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()

    @Before
    fun setUp() {
        sut = OAuthVerifyViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on view loaded`() {
        sut.viewLoaded()
        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.SelectBalanceStoreOauthConfirm)
    }
}
