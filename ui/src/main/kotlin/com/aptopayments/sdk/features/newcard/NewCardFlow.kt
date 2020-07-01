package com.aptopayments.sdk.features.newcard

import com.aptopayments.mobile.data.card.CardApplication
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.workflowaction.*
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.features.disclaimer.DisclaimerFlow
import com.aptopayments.sdk.features.inputdata.CollectUserDataFlow
import com.aptopayments.sdk.features.issuecard.IssueCardFlow
import com.aptopayments.sdk.features.selectbalancestore.SelectBalanceStoreFlow

internal class NewCardFlow(
    val cardProductId: String,
    val onBack: (Unit) -> Unit,
    val onFinish: (cardId: String) -> Unit
) : Flow() {

    private var cardApplication: CardApplication? = null

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        AptoPlatform.applyToCard(CardProduct(id = cardProductId)) { startCardApplicationResult ->
            startCardApplicationResult.either({ onInitComplete(Either.Left(it)) }) { cardApplication ->
                this.cardApplication = cardApplication
                initFlowFor(cardApplication = cardApplication) { initResult ->
                    initResult.either({ onInitComplete(Either.Left(it)) }) { flow ->
                        setStartElement(element = flow)
                        onInitComplete(Either.Right(Unit))
                    }
                }
            }
        }
    }

    override fun restoreState() = Unit

    //
    // Show next application action flow
    //
    private fun showNextFlow() {
        showLoading()
        updateCardApplication { result ->
            result.either(::handleFailure) { cardApplication ->
                initFlowFor(cardApplication = cardApplication) { initResult ->
                    initResult.either(::handleFailure) { flow ->
                        hideLoading()
                        push(flow = flow)
                    }
                }
            }
        }
    }

    private fun updateCardApplication(onComplete: (Either<Failure, CardApplication>) -> Unit) {
        cardApplication?.let { cardApplication ->
            AptoPlatform.fetchCardApplicationStatus(cardApplication.id) { result ->
                result.either({ onComplete }) { cardApplication ->
                    this.cardApplication = cardApplication
                    onComplete(Either.Right(cardApplication))
                }
            }
        }
    }

    //
    // Next Flow Initialization
    //
    private fun initFlowFor(cardApplication: CardApplication, onComplete: (Either<Failure, Flow>) -> Unit) {
        cardApplication.nextAction?.let { workflowAction ->
            when (workflowAction.actionType) {
                WorkflowActionType.SELECT_BALANCE_STORE -> initBalanceStoreFlow(
                    cardApplication,
                    workflowAction,
                    onComplete
                )
                WorkflowActionType.SHOW_DISCLAIMER -> initDisclaimerFlow(cardApplication, workflowAction, onComplete)
                WorkflowActionType.ISSUE_CARD -> initIssueCardFlow(cardApplication, workflowAction, onComplete)
                WorkflowActionType.COLLECT_USER_DATA -> initCollectUserDataFlow(workflowAction, onComplete)
                else -> {
                }
            }
        }
    }

    //
    // Select Balance Store Flow
    //
    private fun initBalanceStoreFlow(
        cardApplication: CardApplication,
        workflowAction: WorkflowAction,
        onComplete: (Either<Failure, Flow>) -> Unit
    ) {
        (workflowAction.configuration as? WorkflowActionConfigurationSelectBalanceStore)?.let { actionConfiguration ->
            val flow = SelectBalanceStoreFlow(
                actionConfiguration = actionConfiguration,
                cardApplicationId = cardApplication.id,
                onBack = { onBack(Unit) },
                onFinish = { showNextFlow() }
            )
            flow.init { initResult ->
                initResult.either({ onComplete(Either.Left(it)) }) {
                    onComplete(Either.Right(flow))
                }
            }
        }
    }

    //
    // Disclaimer
    //
    private fun initDisclaimerFlow(
        cardApplication: CardApplication,
        workflowAction: WorkflowAction,
        onComplete: (Either<Failure, Flow>) -> Unit
    ) {
        (workflowAction.configuration as? WorkflowActionConfigurationShowDisclaimer)?.let { actionConfiguration ->
            val flow = DisclaimerFlow(
                actionConfiguration = actionConfiguration,
                workflowAction = workflowAction,
                workflowObjectId = cardApplication.workflowObjectId,
                cardApplicationId = cardApplication.id,
                onBack = { onBack },
                onAccept = { showNextFlow() }
            )
            flow.init { initResult ->
                initResult.either({ onComplete(Either.Left(it)) }) {
                    onComplete(Either.Right(flow))
                }
            }
        }
    }

    //
    // Issue card
    //
    private fun initIssueCardFlow(
        cardApplication: CardApplication,
        workflowAction: WorkflowAction,
        onComplete: (Either<Failure, Flow>) -> Unit
    ) {
        val actionConfiguration = workflowAction.configuration as? WorkflowActionConfigurationIssueCard
        val flow = IssueCardFlow(
            actionConfiguration = actionConfiguration,
            cardApplicationId = cardApplication.id,
            onBack = { onBack },
            onFinish = onFinish
        )
        flow.init { initResult ->
            initResult.either({ onComplete(Either.Left(it)) }) {
                onComplete(Either.Right(flow))
            }
        }
    }

    //
    // Collect User Data
    //
    private fun initCollectUserDataFlow(
        workflowAction: WorkflowAction,
        onComplete: (Either<Failure, Flow>) -> Unit
    ) {
        val actionConfiguration = workflowAction.configuration as? WorkflowActionCollectUserData
        actionConfiguration?.let {
            val flow = CollectUserDataFlow(actionConfiguration, { onBack }, { showNextFlow() })
            flow.init { initResult ->
                initResult.either({ onComplete(Either.Left(it)) }) {
                    onComplete(Either.Right(flow))
                }
            }
        }
    }
}
