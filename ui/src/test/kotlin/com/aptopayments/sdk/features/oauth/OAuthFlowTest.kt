package com.aptopayments.sdk.features.oauth

import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.features.oauth.connect.OAuthConnectFragment
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

class OAuthFlowTest : UnitTest() {

    private val mockFragmentFactory: FragmentFactory = mock()

    @BeforeEach
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
    fun `should use the factory to instantiate OAuthConnectFragmentInterface as first fragment`() {
        // Given
        val tag = "OAuthConnectFragment"
        val fragmentDouble = mock<OAuthConnectFragment> { on { TAG } doReturn tag }

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
