package com.aptopayments.sdk.features.issuecard

import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.workflowaction.WorkflowActionConfigurationIssueCard
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable

private const val ISSUE_CARD_TAG = "IssueCardFragment"

internal class IssueCardFlow(
    private val actionConfiguration: WorkflowActionConfigurationIssueCard?,
    private val cardApplicationId: String,
    val onBack: () -> Unit,
    val onFinish: (cardId: String) -> Unit
) : Flow(), IssueCardContract.Delegate {

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment =
            fragmentFactory.issueCardFragment(cardApplicationId, actionConfiguration, ISSUE_CARD_TAG)
        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(ISSUE_CARD_TAG) as? IssueCardContract.View)?.let { it.delegate = this }
    }

    override fun onCardIssuedSucceeded(card: Card) {
        onFinish(card.accountID)
    }
}
