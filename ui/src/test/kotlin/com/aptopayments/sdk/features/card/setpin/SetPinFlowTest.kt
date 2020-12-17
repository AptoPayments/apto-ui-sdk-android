package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.mockito.Mock

private const val CARD_ID = "CARD_ID"

class SetPinFlowTest : AndroidTest() {

    private lateinit var sut: SetCardPinFlow

    @Mock
    private lateinit var mockFragmentFactory: FragmentFactory

    @Mock
    private lateinit var mockSetPinDelegate: SetCardPinContract.Delegate

    @Mock
    private lateinit var mockConfirmPinDelegate: ConfirmCardPinContract.Delegate

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
        sut = SetCardPinFlow(cardId = CARD_ID, onBack = {}, onFinish = {})
    }

    @Test
    fun `should use the factory to instantiate SetPinFragmentInterface as first fragment`() {
        // Given
        val tag = "SetCardPinFragment"
        val fragmentDouble = SetPinFragmentDouble(mockSetPinDelegate).apply { this.TAG = tag }
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
        val fragmentDouble = ConfirmPinFragmentDouble(mockConfirmPinDelegate).apply { this.TAG = tag }
        given {
            mockFragmentFactory.confirmPinFragment(cardId = CARD_ID, pin = pin, tag = tag)
        }.willReturn(fragmentDouble)

        // When
        sut.setPinFinished(pin)

        // Then
        verify(mockFragmentFactory).confirmPinFragment(cardId = CARD_ID, pin = pin, tag = tag)
    }
}
