package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.sdk.repository.CardMetadataRepository
import com.aptopayments.sdk.repository.ForceIssueCardRepository
import com.aptopayments.sdk.repository.ManageCardIdRepository
import com.aptopayments.sdk.repository.UserMetadataRepository

internal class SaveInitializationDataUseCase(
    private val cardMetadataRepository: CardMetadataRepository,
    private val userMetadataRepository: UserMetadataRepository,
    private val manageCardIdRepository: ManageCardIdRepository,
    private val forceApplyToCardRepository: ForceIssueCardRepository
) : UseCase<Unit, SaveInitializationDataUseCase.InitializationData?>() {

    override fun run(params: InitializationData?): Either<Failure, Unit> {
        cardMetadataRepository.data = params?.cardMetadata
        userMetadataRepository.data = params?.userMetadata
        manageCardIdRepository.data = params?.manageCardId
        forceApplyToCardRepository.data = params?.forceApplyToCard ?: false
        return Unit.right()
    }

    data class InitializationData(
        val userMetadata: String? = null,
        val cardMetadata: String? = null,
        val manageCardId: String? = null,
        val forceApplyToCard: Boolean = false
    )
}
