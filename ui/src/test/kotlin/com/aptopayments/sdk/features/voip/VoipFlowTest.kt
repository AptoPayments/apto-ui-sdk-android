package com.aptopayments.sdk.features.voip

import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.voip.Action
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

class VoipFlowTest : UnitTest() {

    private lateinit var sut: VoipFlow

    private val mockFragmentFactory: FragmentFactory = mock()
    private val mockDelegate: VoipContract.Delegate = mock()
    private val mockAction: Action = mock()
    private val cardId = "TEST_CARD_ID"

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
        sut = VoipFlow(cardId = cardId, onBack = {}, onFinish = {}, action = mockAction)
    }

    @Test
    fun `should use the factory to instantiate VoipFragmentInterface as first fragment`() {
        // Given
        val tag = "VoipFragment"
        val fragmentDouble = VoipFragmentDouble(mockDelegate).apply { this.TAG = tag }
        given {
            mockFragmentFactory.getVoipFragment(tag = tag, action = mockAction, cardId = cardId)
        }.willReturn(fragmentDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).getVoipFragment(tag = tag, action = mockAction, cardId = cardId)
    }
}
