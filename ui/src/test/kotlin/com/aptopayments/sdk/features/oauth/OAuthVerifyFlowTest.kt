package com.aptopayments.sdk.features.oauth

import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.features.oauth.verify.OAuthVerifyContract
import com.aptopayments.sdk.features.oauth.verify.OAuthVerifyFragmentDouble
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.mockito.Mock

class OAuthVerifyFlowTest : AndroidTest() {

    @Mock
    private lateinit var mockFragmentFactory: FragmentFactory
    @Mock
    private lateinit var mockDelegate: OAuthVerifyContract.Delegate

    @Before
    override fun setUp() {
        super.setUp()
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
        startKoin {
            modules(
                module {
                    single { mockFragmentFactory }
                }
            )
        }
    }

    @Test
    fun `should use the factory to instantiate OAuthVerifyFragmentInterface as first fragment`() {
        // Given
        val tag = "OAuthVerifyFragment"
        val fragmentDouble = OAuthVerifyFragmentDouble(mockDelegate).apply { this.TAG = tag }
        val allowedBalanceType = TestDataProvider.provideAllowedBalanceType()
        val oAuthAttempt = TestDataProvider.provideOAuthAttempt()
        val sut = OAuthVerifyFlow(
            allowedBalanceType = allowedBalanceType, oauthAttempt = oAuthAttempt,
            onBack = {}, onFinish = {}, onError = {}
        )
        given {
            mockFragmentFactory.oauthVerifyFragment(
                datapoints = oAuthAttempt.userData!!,
                allowedBalanceType = allowedBalanceType,
                tokenId = oAuthAttempt.tokenId,
                tag = tag
            )
        }.willReturn(fragmentDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).oauthVerifyFragment(
            datapoints = oAuthAttempt.userData!!,
            allowedBalanceType = allowedBalanceType,
            tokenId = oAuthAttempt.tokenId,
            tag = tag
        )
    }
}
