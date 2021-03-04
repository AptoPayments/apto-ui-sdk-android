package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.fundingsources.Balance
import com.aptopayments.mobile.data.fundingsources.AchAccountDetails
import com.aptopayments.mobile.data.user.agreements.AgreementAction
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.flatMapSuspending
import com.aptopayments.mobile.functional.left
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class AcceptAchDisclaimerUseCase(private val aptoPlatform: AptoPlatformProtocol) :
    UseCaseAsync<AchAccountDetails, AcceptAchDisclaimerUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, AchAccountDetails> {
        return fetchCard(params.cardId)
            .flatMapSuspending { card ->
                card.features?.achAccount?.disclaimer?.let {
                    reviewAgreements(it.keys)
                } ?: NoDisclaimerFailure().left()
            }
            .flatMapSuspending { getBalance(params.cardId) }
            .flatMapSuspending { createAccount(balanceId = it.id) }
    }

    private suspend fun fetchCard(cardId: String) = suspendCoroutine<Either<Failure, Card>> { cont ->
        aptoPlatform.fetchCard(cardId = cardId, forceRefresh = false) { cont.resume(it) }
    }

    private suspend fun reviewAgreements(agreements: List<String>) = suspendCoroutine<Either<Failure, Unit>> { cont ->
        aptoPlatform.reviewAgreements(agreements, AgreementAction.ACCEPTED) {
            cont.resume(it)
        }
    }

    private suspend fun getBalance(cardId: String) = suspendCoroutine<Either<Failure, Balance>> { cont ->
        aptoPlatform.fetchCardFundingSource(cardId, false) { cont.resume(it) }
    }

    private suspend fun createAccount(balanceId: String) =
        suspendCoroutine<Either<Failure, AchAccountDetails>> { cont ->
            aptoPlatform.assignAchAccount(balanceId) { cont.resume(it) }
        }

    data class Params(val cardId: String)

    class NoDisclaimerFailure : Failure.FeatureFailure()
}
