package com.aptopayments.sdk.features.addbalance

import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.features.oauth.OAuthConfig
import com.aptopayments.sdk.features.oauth.connect.OAuthConnectContract
import com.aptopayments.sdk.features.oauth.connect.OAuthConnectFragmentDouble
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.test.assertEquals

class AddBalanceFlowTest : UnitTest() {
    private val mockFragmentFactory: FragmentFactory = mock()
    private val mockDelegate: OAuthConnectContract.Delegate = mock()

    private val allowedBalanceTypes = listOf(TestDataProvider.provideAllowedBalanceType())

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
    fun `init starts the oauth flow with appropriate values`() {
        // Given
        val sut = AddBalanceFlow(allowedBalanceTypes = allowedBalanceTypes, cardID = "card", onBack = {}, onFinish = {})
        val errorMessageKeys = listOf(
            "external_auth.login.error_oauth_invalid_request.message",
            "external_auth.login.error_oauth_unauthorised_client.message",
            "external_auth.login.error_oauth_access_denied.message",
            "external_auth.login.error_oauth_unsupported_response_type.message",
            "external_auth.login.error_oauth_invalid_scope.message",
            "external_auth.login.error_oauth_server_error.message",
            "external_auth.login.error_oauth_temporarily_unavailable.message",
            "external_auth.login.error_oauth_unknown.message"
        )
        val fragmentDouble = OAuthConnectFragmentDouble(mockDelegate).apply { this.TAG = "OAuthConnectFragment" }
        val captor = argumentCaptor<OAuthConfig>()
        given {
            mockFragmentFactory.oauthConnectFragment(
                config = captor.capture(),
                tag = TestDataProvider.anyObject()
            )
        }.willReturn(fragmentDouble)

        // When
        sut.init {}

        // Then
        assertEquals(errorMessageKeys, captor.firstValue.errorMessageKeys)
    }
}
