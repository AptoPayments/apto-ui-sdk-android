package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.repository.ForceIssueCardRepository
import com.aptopayments.sdk.repository.ManageCardIdRepository
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class InitNewOrExistingFlowUseCase(
    private val aptoPlatform: AptoPlatformProtocol,
    private val manageCardIdRepository: ManageCardIdRepository,
    private val forceIssueCardRepository: ForceIssueCardRepository
) : UseCaseAsyncWithoutParams<InitNewOrExistingFlowUseCase.Action>() {

    override suspend fun run(): Either<Failure, Action> {
        val initWithCard = manageCardIdRepository.data
        return when {
            initWithCard != null -> fetchCardAndSendAction(initWithCard)
            forceIssueCardRepository.data -> continueToCardProductSelector()
            else -> fetchCardListAndDecideAction()
        }
    }

    private fun continueToCardProductSelector(): Either<Nothing, Action.ContinueWithCardProductSelectorFlow> {
        forceIssueCardRepository.clear()
        return Action.ContinueWithCardProductSelectorFlow.right()
    }

    private suspend fun fetchCardAndSendAction(initWithCard: String): Either<Failure, Action.ContinueFlowWithCard> {
        return fetchCard(initWithCard).map {
            Action.ContinueFlowWithCard(it.accountID)
        }
    }

    private suspend fun fetchCardListAndDecideAction(): Either<Failure, Action> {
        return fetchCardList().map { list -> list.firstOrNull { it.state != Card.CardState.CANCELLED } }.map {
            if (it != null) {
                Action.ContinueFlowWithCard(it.accountID)
            } else {
                Action.ContinueWithCardProductSelectorFlow
            }
        }
    }

    private suspend fun fetchCard(cardId: String) = suspendCoroutine<Either<Failure, Card>> { cont ->
        aptoPlatform.fetchCard(cardId = cardId, forceRefresh = true) { cont.resume(it) }
    }

    private suspend fun fetchCardList() = suspendCoroutine<Either<Failure, List<Card>>> { cont ->
        aptoPlatform.fetchCards(null) { result -> cont.resume(result.map { it.data }) }
    }

    sealed class Action {
        class ContinueFlowWithCard(val cardId: String) : Action()
        object ContinueWithCardProductSelectorFlow : Action()
    }
}
