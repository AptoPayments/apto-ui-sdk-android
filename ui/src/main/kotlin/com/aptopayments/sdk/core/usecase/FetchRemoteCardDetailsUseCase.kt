package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.data.card.CardDetails
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.usecase.FetchRemoteCardDetailsUseCase.Params
import com.aptopayments.sdk.repository.LocalCardDetailsRepository
import com.aptopayments.sdk.repository.RemoteCardDetailsRepository

internal class FetchRemoteCardDetailsUseCase(
    private val localCardDetailsRepository: LocalCardDetailsRepository,
    private val remoteCardDetailsRepository: RemoteCardDetailsRepository
) : UseCaseAsync<CardDetails, Params>() {

    override suspend fun run(params: Params): Either<Failure, CardDetails> {
        val details = remoteCardDetailsRepository.fetch(params.cardId)
        details.either({}, { result -> localCardDetailsRepository.saveCardDetails(result) })
        return details
    }

    data class Params(
        val cardId: String
    )
}
