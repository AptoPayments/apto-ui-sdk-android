package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.config.UITheme
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

class SetPinFlowTest : AndroidTest() {

    private lateinit var sut: SetPinFlow
    @Mock private lateinit var mockFragmentFactory: FragmentFactory
    @Mock private lateinit var mockSetPinDelegate: SetPinContract.Delegate
    @Mock private lateinit var mockConfirmPinDelegate: ConfirmPinContract.Delegate

    @Before
    override fun setUp() {
        super.setUp()
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
        startKoin {
            modules(module {
                single { mockFragmentFactory }
            })
        }
        sut = SetPinFlow(cardId = "TEST_CARD_ID", onBack = {}, onFinish = {})
    }

    @Test
    fun `should use the factory to instantiate SetPinFragmentInterface as first fragment`() {
        // Given
        val tag = "SetPinFragment"
        val fragmentDouble = SetPinFragmentDouble(mockSetPinDelegate).apply { this.TAG = tag }
        given { mockFragmentFactory.setPinFragment(uiTheme = UITheme.THEME_1, tag = tag)
        }.willReturn(fragmentDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).setPinFragment(uiTheme = UITheme.THEME_1, tag = tag)
    }

    @Test
    fun `should use the factory to instantiate ConfirmPinFragmentInterface when set pin has finished`() {
        // Given
        val tag = "ConfirmPinFragment"
        val pin = "1234"
        val fragmentDouble = ConfirmPinFragmentDouble(mockConfirmPinDelegate).apply { this.TAG = tag }
        given { mockFragmentFactory.confirmPinFragment(uiTheme = UITheme.THEME_1, pin = pin, tag = tag)
        }.willReturn(fragmentDouble)

        // When
        sut.setPinFinished(pin)

        // Then
        verify(mockFragmentFactory).confirmPinFragment(uiTheme = UITheme.THEME_1, pin = pin, tag = tag)
    }
}
