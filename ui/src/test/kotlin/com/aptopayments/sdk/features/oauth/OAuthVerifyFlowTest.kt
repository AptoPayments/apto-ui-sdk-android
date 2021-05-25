package com.aptopayments.sdk.features.oauth

import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.features.oauth.verify.OAuthVerifyFragment
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

class OAuthVerifyFlowTest : UnitTest() {

    private val mockFragmentFactory: FragmentFactory = mock()

    @Before
    fun setUp() {
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
        val fragmentDouble = mock<OAuthVerifyFragment> { on { TAG } doReturn tag }
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
