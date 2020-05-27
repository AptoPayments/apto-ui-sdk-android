package com.aptopayments.sdk.features.oauth

import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.features.oauth.connect.OAuthConnectContract
import com.aptopayments.sdk.features.oauth.connect.OAuthConnectFragmentDouble
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.mockito.Mock

class OAuthFlowTest : AndroidTest() {

    @Mock
    private lateinit var mockFragmentFactory: FragmentFactory
    @Mock
    private lateinit var mockDelegate: OAuthConnectContract.Delegate

    @Before
    override fun setUp() {
        super.setUp()
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
        startKoin {
            modules(module {
                single { mockFragmentFactory }
            })
        }
    }

    @Test
    fun `should use the factory to instantiate OAuthConnectFragmentInterface as first fragment`() {
        // Given
        val tag = "OAuthConnectFragment"
        val fragmentDouble = OAuthConnectFragmentDouble(mockDelegate).apply { this.TAG = tag }
        val config = TestDataProvider.provideOauthConfig()
        val sut = OAuthFlow(config = config, onBack = {}, onFinish = {})
        given {
            mockFragmentFactory.oauthConnectFragment(config = config, tag = tag)
        }.willReturn(fragmentDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).oauthConnectFragment(config = config, tag = tag)
    }
}
