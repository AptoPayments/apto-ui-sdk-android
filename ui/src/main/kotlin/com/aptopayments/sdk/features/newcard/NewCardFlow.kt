package com.aptopayments.sdk.features.newcard

import com.aptopayments.mobile.data.card.CardApplication
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.workflowaction.WorkflowAction
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.left
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.features.disclaimer.DisclaimerFlow
import com.aptopayments.sdk.features.inputdata.CollectUserDataFlow
import com.aptopayments.sdk.features.issuecard.IssueCardFlow
import com.aptopayments.sdk.features.selectbalancestore.SelectBalanceStoreFlow
import com.aptopayments.sdk.repository.CardMetadataRepository
import org.koin.core.inject

internal class NewCardFlow(
    val cardProductId: String,
    val onBack: () -> Unit,
    val onFinish: (cardId: String) -> Unit
) : Flow() {
    private val cardMetadataRepository: CardMetadataRepository by inject()

    private var cardApplication: CardApplication? = null

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        AptoPlatform.applyToCard(
            CardProduct(id = cardProductId)
        ) { startCardApplicationResult ->
            startCardApplicationResult.either({ onInitComplete(Either.Left(it)) }) { cardApplication ->
                this.cardApplication = cardApplication
                cardMetadataRepository.clear()
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
                result.either({ onComplete(it.left()) }) { cardApplication ->
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
            when (workflowAction) {
                is WorkflowAction.SelectBalanceStoreAction -> initBalanceStoreFlow(
                    cardApplication,
                    workflowAction,
                    onComplete
                )
                is WorkflowAction.ShowDisclaimerAction -> initDisclaimerFlow(cardApplication, workflowAction, onComplete)
                is WorkflowAction.IssueCardAction -> initIssueCardFlow(cardApplication, workflowAction, onComplete)
                is WorkflowAction.CollectUserDataAction -> initCollectUserDataFlow(workflowAction, onComplete)
                else -> onComplete((object : Failure.FeatureFailure() {}).left())
            }
        }
    }

    //
    // Select Balance Store Flow
    //
    private fun initBalanceStoreFlow(
        cardApplication: CardApplication,
        workflowAction: WorkflowAction.SelectBalanceStoreAction,
        onComplete: (Either<Failure, Flow>) -> Unit
    ) {
        workflowAction.configuration?.let { actionConfiguration ->
            val flow = SelectBalanceStoreFlow(
                actionConfiguration = actionConfiguration,
                cardApplicationId = cardApplication.id,
                onBack = { onBack.invoke() },
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
        workflowAction: WorkflowAction.ShowDisclaimerAction,
        onComplete: (Either<Failure, Flow>) -> Unit
    ) {
        workflowAction.configuration?.let { actionConfiguration ->
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
        workflowAction: WorkflowAction.IssueCardAction,
        onComplete: (Either<Failure, Flow>) -> Unit
    ) {
        val actionConfiguration = workflowAction.configuration
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
        workflowAction: WorkflowAction.CollectUserDataAction,
        onComplete: (Either<Failure, Flow>) -> Unit
    ) {
        val actionConfiguration = workflowAction.configuration
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
