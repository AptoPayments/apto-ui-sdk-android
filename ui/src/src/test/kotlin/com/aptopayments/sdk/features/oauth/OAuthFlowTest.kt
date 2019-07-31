package com.aptopayments.sdk.features.oauth

import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.config.UITheme
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.features.oauth.connect.OAuthConnectContract
import com.aptopayments.sdk.features.oauth.connect.OAuthConnectFragmentDouble
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

class OAuthFlowTest: AndroidTest() {

    @Mock private lateinit var mockFragmentFactory: FragmentFactory
    @Mock private lateinit var mockDelegate: OAuthConnectContract.Delegate

    @Before
    override fun setUp() {
        super.setUp()
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
    }

    @Test
    fun `should use the factory to instantiate OAuthConnectFragmentInterface as first fragment`() {

        // Given
        val tag = "OAuthConnectFragment"
        val fragmentDouble = OAuthConnectFragmentDouble(mockDelegate).apply { this.TAG = tag }
        val allowedBalanceType = TestDataProvider.provideAllowedBalanceType()
        val sut = OAuthFlow(allowedBalanceType = allowedBalanceType, onBack = {}, onFinish = {})
        given { mockFragmentFactory.oauthConnectFragment(
                uiTheme = UITheme.THEME_1,
                allowedBalanceType = allowedBalanceType,
                tag = tag)
        }.willReturn(fragmentDouble)

        // When
        sut.fragmentFactory = mockFragmentFactory
        sut.allowedBalanceType = allowedBalanceType
        sut.init {}

        // Then
        verify(mockFragmentFactory).oauthConnectFragment(
                uiTheme = UITheme.THEME_1,
                allowedBalanceType = allowedBalanceType,
                tag = tag)
    }

}
