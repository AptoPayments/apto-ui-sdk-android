package com.aptopayments.sdk.features.oauth.connect

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test

class OAuthConnectViewModelTest : UnitTest() {

    private lateinit var sut: OAuthConnectViewModel

    private var analyticsManager: AnalyticsServiceContract = mock()

    @Before
    fun setUp() {
        sut = OAuthConnectViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on init`() {
        verify(analyticsManager).track(Event.SelectBalanceStoreOauthLogin)
    }
}
