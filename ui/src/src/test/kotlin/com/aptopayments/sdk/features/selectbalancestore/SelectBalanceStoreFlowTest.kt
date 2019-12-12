package com.aptopayments.sdk.features.selectbalancestore

import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.card.SelectBalanceStoreResult
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.workflowaction.WorkflowActionConfigurationSelectBalanceStore
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.core.platform.AptoPlatformProtocol
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Spy
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SelectBalanceStoreFlowTest : UnitTest() {

    private lateinit var sut: SelectBalanceStoreFlow
    @Mock private lateinit var mockFragmentFactory: FragmentFactory
    private val cardApplicationId = "TEST_CARD_APPLICATION_ID"
    @Spy
    private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()
    @Mock private lateinit var mockAptoPlatform: AptoPlatform

    @Before
    fun setUp() {
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
        startKoin {
            modules(module {
                single { mockFragmentFactory }
                single<AnalyticsServiceContract> { analyticsManager }
                single<AptoPlatformProtocol> { mockAptoPlatform }
            })
        }
        val testAllowedBalanceType = TestDataProvider.provideAllowedBalanceType()
        val testAllowedBalanceTypeList = listOf(testAllowedBalanceType)
        val mockActionConfig = WorkflowActionConfigurationSelectBalanceStore(testAllowedBalanceTypeList, null)
        sut = SelectBalanceStoreFlow(actionConfiguration = mockActionConfig,
                cardApplicationId = cardApplicationId, onBack = {}, onFinish = {})
    }

    @Test
    fun `should start the OAuth flow if config contains an allowed balance type`() {
        // Given
        val sutSpy = Mockito.spy(sut)
        Mockito.doNothing().`when`(sutSpy).initOAuthFlow(TestDataProvider.anyObject(), TestDataProvider.anyObject(),
                TestDataProvider.anyObject())

        // When
        sutSpy.init {}

        // Then
        verify(sutSpy).initOAuthFlow(TestDataProvider.anyObject(), TestDataProvider.anyObject(),
                TestDataProvider.anyObject())
    }

    @Test
    fun `should track event selectBalanceStoreIdentityNotVerified when select balance store fails`() {
        // Given
        val result = SelectBalanceStoreResult(
                result = SelectBalanceStoreResult.Type.INVALID,
                errorCode = 200046)
        Mockito.`when`(mockAptoPlatform.setBalanceStore(anyString(), anyString(), TestDataProvider.anyObject())).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, SelectBalanceStoreResult>) -> Unit).invoke(Either.Right(result))
        }

        // When
        sut.selectBalanceStore(TestDataProvider.provideOAuthAttempt())

        // Then
        assertTrue(analyticsManager.trackCalled)
        assertEquals(analyticsManager.lastEvent, Event.SelectBalanceStoreOauthConfirmIdentityNotVerified)
    }
}
