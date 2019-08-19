package com.aptopayments.sdk.features.voip

import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.config.UITheme
import com.aptopayments.core.data.voip.Action
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.mockito.Mock

class VoipFlowTest : AndroidTest() {

    private lateinit var sut: VoipFlow
    @Mock private lateinit var mockFragmentFactory: FragmentFactory
    @Mock private lateinit var mockDelegate: VoipContract.Delegate
    @Mock private lateinit var mockAction: Action
    private val cardId = "TEST_CARD_ID"

    @Before
    override fun setUp() {
        super.setUp()
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
        startKoin {
            modules(module {
                single { mockFragmentFactory }
            })
        }
        sut = VoipFlow(cardId = cardId, onBack = {}, onFinish = {}, action = mockAction)
    }

    @Test
    fun `should use the factory to instantiate VoipFragmentInterface as first fragment`() {
        // Given
        val tag = "VoipFragment"
        val fragmentDouble = VoipFragmentDouble(mockDelegate).apply { this.TAG = tag }
        given { mockFragmentFactory.getVoipFragment(uiTheme = UITheme.THEME_1, tag = tag, action = mockAction, cardId = cardId)
        }.willReturn(fragmentDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).getVoipFragment(uiTheme = UITheme.THEME_1, tag = tag, action = mockAction, cardId = cardId)
    }
}
