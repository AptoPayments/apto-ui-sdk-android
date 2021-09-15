package com.aptopayments.sdk.features.oauth.connect

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OAuthConnectViewModelTest : UnitTest() {

    private lateinit var sut: OAuthConnectViewModel

    private var analyticsManager: AnalyticsServiceContract = mock()

    @BeforeEach
    fun setUp() {
        sut = OAuthConnectViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on init`() {
        verify(analyticsManager).track(Event.SelectBalanceStoreOauthLogin)
    }
}
