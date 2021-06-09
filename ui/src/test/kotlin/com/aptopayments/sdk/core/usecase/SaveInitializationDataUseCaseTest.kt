package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.data.InitializationData
import com.aptopayments.sdk.repository.ForceIssueCardRepository
import com.aptopayments.sdk.repository.InMemoryInitializationDataRepository
import com.aptopayments.sdk.repository.ManageCardIdRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private const val CARD_METADATA = "card"
private const val USER_METADATA = "user"
private const val UID = "uid"
private const val CARD_ID = "id_1234"

internal class SaveInitializationDataUseCaseTest {

    private val initializationDataRepository: InMemoryInitializationDataRepository = spy()
    private val manageCardIdRepository: ManageCardIdRepository = mock()
    private val applyToCardRepository: ForceIssueCardRepository = mock()

    private val sut =
        SaveFlowConfigurationDataUseCase(
            initializationDataRepository,
            manageCardIdRepository,
            applyToCardRepository
        )

    @Test
    fun `when null params then all repositories set to null`() {
        sut(null)

        verify(initializationDataRepository).data = null
        verify(manageCardIdRepository).data = null
        verify(applyToCardRepository).data = false
    }

    @Test
    fun `when manageCardId set then is saved correctly `() {
        val data = SaveFlowConfigurationDataUseCase.Params(manageCardId = CARD_ID)

        sut(data)

        verify(manageCardIdRepository).data = CARD_ID
    }

    @Test
    fun `when cardMetadata set then is saved correctly `() {
        val data = SaveFlowConfigurationDataUseCase.Params(
            initializationData = InitializationData(cardMetadata = CARD_METADATA)
        )

        sut(data)

        assertEquals(CARD_METADATA, initializationDataRepository.data?.cardMetadata)
    }

    @Test
    fun `when userMetadata set then is saved correctly `() {
        val data = SaveFlowConfigurationDataUseCase.Params(
            initializationData = InitializationData(userMetadata = USER_METADATA)
        )

        sut(data)

        assertEquals(USER_METADATA, initializationDataRepository.data?.userMetadata)
    }

    @Test
    fun `when custodianUid set then is saved correctly `() {
        val data = SaveFlowConfigurationDataUseCase.Params(
            initializationData = InitializationData(custodianUid = UID)
        )

        sut(data)

        assertEquals(UID, initializationDataRepository.data?.custodianUid)
    }

    @Test
    fun `when applyToCard set then is saved correctly `() {
        val data = SaveFlowConfigurationDataUseCase.Params(forceApplyToCard = true)

        sut(data)

        verify(applyToCardRepository).data = true
    }
}
