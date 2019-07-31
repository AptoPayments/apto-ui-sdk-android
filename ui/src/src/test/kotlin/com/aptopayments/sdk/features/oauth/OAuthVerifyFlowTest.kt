package com.aptopayments.sdk.features.oauth

import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.config.UITheme
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.features.oauth.verify.OAuthVerifyContract
import com.aptopayments.sdk.features.oauth.verify.OAuthVerifyFragmentDouble
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

class OAuthVerifyFlowTest: AndroidTest() {

    @Mock private lateinit var mockFragmentFactory: FragmentFactory
    @Mock private lateinit var mockDelegate: OAuthVerifyContract.Delegate

    @Before
    override fun setUp() {
        super.setUp()
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
    }

    @Test
    fun `should use the factory to instantiate OAuthVerifyFragmentInterface as first fragment`() {

        // Given
        val tag = "OAuthVerifyFragment"
        val fragmentDouble = OAuthVerifyFragmentDouble(mockDelegate).apply { this.TAG = tag }
        val allowedBalanceType = TestDataProvider.provideAllowedBalanceType()
        val oAuthAttempt = TestDataProvider.provideOAuthAttempt()
        val sut = OAuthVerifyFlow(allowedBalanceType = allowedBalanceType, oauthAttempt = oAuthAttempt,
                onBack = {}, onFinish = {}, onError = {})
        given { mockFragmentFactory.oauthVerifyFragment(
                uiTheme = UITheme.THEME_1,
                datapoints = oAuthAttempt.userData!!,
                allowedBalanceType = allowedBalanceType,
                tokenId = oAuthAttempt.tokenId,
                tag = tag)
        }.willReturn(fragmentDouble)

        // When
        sut.fragmentFactory = mockFragmentFactory
        sut.init {}

        // Then
        verify(mockFragmentFactory).oauthVerifyFragment(
                uiTheme = UITheme.THEME_1,
                datapoints = oAuthAttempt.userData!!,
                allowedBalanceType = allowedBalanceType,
                tokenId = oAuthAttempt.tokenId,
                tag = tag)
    }

}
