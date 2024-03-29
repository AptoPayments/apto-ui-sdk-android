package com.aptopayments.sdk.features.voip

import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.voip.Action
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

class VoipFlowTest : UnitTest() {

    private lateinit var sut: VoipFlow

    private val mockFragmentFactory: FragmentFactory = mock()
    private val mockAction: Action = mock()
    private val cardId = "TEST_CARD_ID"

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
        sut = VoipFlow(cardId = cardId, onBack = {}, onFinish = {}, action = mockAction)
    }

    @Test
    fun `should use the factory to instantiate VoipFragmentInterface as first fragment`() {
        // Given
        val tag = "VoipFragment"
        val fragmentDouble = mock<VoipFragment> { on { TAG } doReturn tag }
        given {
            mockFragmentFactory.getVoipFragment(tag = tag, action = mockAction, cardId = cardId)
        }.willReturn(fragmentDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).getVoipFragment(tag = tag, action = mockAction, cardId = cardId)
    }
}
