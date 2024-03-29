package com.aptopayments.sdk.features.oauth.verify

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OAuthVerifyViewModelTest {

    private lateinit var sut: OAuthVerifyViewModel

    private var analyticsManager: AnalyticsServiceContract = mock()

    @BeforeEach
    fun setUp() {
        sut = OAuthVerifyViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on init`() {
        verify(analyticsManager).track(Event.SelectBalanceStoreOauthConfirm)
    }
}
