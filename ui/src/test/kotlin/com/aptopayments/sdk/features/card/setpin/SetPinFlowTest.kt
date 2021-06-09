package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

private const val CARD_ID = "CARD_ID"

class SetPinFlowTest : UnitTest() {

    private lateinit var sut: SetCardPinFlow

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
        sut = SetCardPinFlow(cardId = CARD_ID, onBack = {}, onFinish = {})
    }

    @Test
    fun `should use the factory to instantiate SetPinFragmentInterface as first fragment`() {
        // Given
        val tag = "SetCardPinFragment"
        val fragmentDouble = mock<SetCardPinFragment> { on { TAG } doReturn tag }

        given {
            mockFragmentFactory.setPinFragment(tag)
        }.willReturn(fragmentDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).setPinFragment(tag)
    }

    @Test
    fun `should use the factory to instantiate ConfirmPinFragmentInterface when set pin has finished`() {
        // Given
        val tag = "ConfirmCardPinFragment"
        val pin = "1234"
        val fragmentDouble = mock<ConfirmCardPinFragment> { on { TAG } doReturn tag }
        given {
            mockFragmentFactory.confirmPinFragment(cardId = CARD_ID, pin = pin, tag = tag)
        }.willReturn(fragmentDouble)

        // When
        sut.setPinFinished(pin)

        // Then
        verify(mockFragmentFactory).confirmPinFragment(cardId = CARD_ID, pin = pin, tag = tag)
    }
}
