package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.repository.CardMetadataRepository
import com.aptopayments.sdk.repository.ForceIssueCardRepository
import com.aptopayments.sdk.repository.ManageCardIdRepository
import com.aptopayments.sdk.repository.UserMetadataRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

private const val CARD_METADATA = "card"
private const val USER_METADATA = "user"
private const val CARD_ID = "id_1234"

internal class SaveInitializationDataUseCaseTest {

    private val cardMetadataRepository: CardMetadataRepository = mock()
    private val userMetadataRepository: UserMetadataRepository = mock()
    private val manageCardIdRepository: ManageCardIdRepository = mock()
    private val applyToCardRepository: ForceIssueCardRepository = mock()

    private val sut =
        SaveInitializationDataUseCase(
            cardMetadataRepository,
            userMetadataRepository,
            manageCardIdRepository,
            applyToCardRepository
        )

    @Test
    fun `when null params then all repositories set to null`() {
        sut(null)

        verify(cardMetadataRepository).data = null
        verify(userMetadataRepository).data = null
        verify(manageCardIdRepository).data = null
        verify(applyToCardRepository).data = false
    }

    @Test
    fun `when manageCardId set then is saved correctly `() {
        val data = SaveInitializationDataUseCase.InitializationData(manageCardId = CARD_ID)

        sut(data)

        verify(manageCardIdRepository).data = CARD_ID
    }

    @Test
    fun `when cardMetadata set then is saved correctly `() {
        val data = SaveInitializationDataUseCase.InitializationData(cardMetadata = CARD_METADATA)

        sut(data)

        verify(cardMetadataRepository).data = CARD_METADATA
    }

    @Test
    fun `when userMetadata set then is saved correctly `() {
        val data = SaveInitializationDataUseCase.InitializationData(userMetadata = USER_METADATA)

        sut(data)

        verify(userMetadataRepository).data = USER_METADATA
    }

    @Test
    fun `when applyToCard set then is saved correctly `() {
        val data = SaveInitializationDataUseCase.InitializationData(forceApplyToCard = true)

        sut(data)

        verify(applyToCardRepository).data = true
    }
}
