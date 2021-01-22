package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.sdk.data.InitializationData
import com.aptopayments.sdk.repository.*
import com.aptopayments.sdk.repository.ManageCardIdRepository

internal class SaveFlowConfigurationDataUseCase(
    private val initializationDataRepository: InitializationDataRepository,
    private val manageCardIdRepository: ManageCardIdRepository,
    private val forceApplyToCardRepository: ForceIssueCardRepository
) : UseCase<Unit, SaveFlowConfigurationDataUseCase.Params?>() {

    override fun run(params: Params?): Either<Failure, Unit> {
        initializationDataRepository.data = params?.initializationData
        manageCardIdRepository.data = params?.manageCardId
        forceApplyToCardRepository.data = params?.forceApplyToCard ?: false
        return Unit.right()
    }

    data class Params(
        val initializationData: InitializationData? = null,
        val manageCardId: String? = null,
        val forceApplyToCard: Boolean = false
    )
}
