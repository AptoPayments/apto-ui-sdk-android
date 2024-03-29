package com.aptopayments.sdk.core.platform

import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.usecase.SaveFlowConfigurationDataUseCase
import com.aptopayments.sdk.core.usecase.SaveFlowConfigurationDataUseCase.Params
import com.aptopayments.sdk.data.InitializationData
import com.aptopayments.sdk.features.card.CardFlow
import org.mockito.kotlin.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

class AptoUiSdkTest : UnitTest() {

    private val cardFlow: CardFlow = mock()
    private val initializationData: InitializationData = mock()

    @BeforeEach
    fun setUp() {
        AptoPlatform.koin = startKoin {
            modules(
                listOf(
                    module {
                        factory(override = true) { cardFlow }
                        factory(override = true) { saveInitializationDataUseCase }
                    }
                )
            )
        }.koin
    }

    @Test
    fun `by default DarkTheme is set to false`() {
        assertFalse(AptoUiSdk.cardOptions.darkThemeEnabled())
    }

    @Test
    fun `given initializationData whenever startCardFlow then saveInitializationData is called with correct parameters`() {
        whenever(saveInitializationDataUseCase.run(any())).thenReturn(mock())

        AptoUiSdk.startCardFlow(mock(), CardOptions(), initializationData, null, null)

        verify(saveInitializationDataUseCase).invoke(Params(initializationData = initializationData))
    }

    @Test
    fun `given initializationData and cardId whenever startManageCardFlow then saveInitializationData is called with correct parameters`() {
        val cardId = "card_id"
        whenever(saveInitializationDataUseCase.run(any())).thenReturn(mock())

        AptoUiSdk.startManageCardFlow(mock(), CardOptions(), cardId, initializationData, null, null)

        verify(saveInitializationDataUseCase).invoke(
            Params(
                initializationData = initializationData,
                manageCardId = cardId
            )
        )
    }

    @Test
    fun `given initializationData whenever startCardApplicationFlow then saveInitializationData is called with correct parameters`() {
        whenever(saveInitializationDataUseCase.invoke(any())).thenReturn(mock())

        AptoUiSdk.startCardApplicationFlow(mock(), CardOptions(), initializationData, null, null)

        verify(saveInitializationDataUseCase).invoke(
            Params(
                initializationData = initializationData,
                forceApplyToCard = true
            )
        )
    }

    companion object {
        private val saveInitializationDataUseCase: SaveFlowConfigurationDataUseCase = mock()
    }
}
