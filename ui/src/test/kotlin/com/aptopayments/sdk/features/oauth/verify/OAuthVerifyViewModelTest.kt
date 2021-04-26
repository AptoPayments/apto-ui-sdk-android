package com.aptopayments.sdk.features.oauth.verify

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test

class OAuthVerifyViewModelTest {

    private lateinit var sut: OAuthVerifyViewModel

    private var analyticsManager: AnalyticsServiceContract = mock()

    @Before
    fun setUp() {
        sut = OAuthVerifyViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on init`() {
        verify(analyticsManager).track(Event.SelectBalanceStoreOauthConfirm)
    }
}
