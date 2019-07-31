package com.aptopayments.sdk.features.issuecard

import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.workflowaction.WorkflowActionConfigurationIssueCard
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import java.lang.reflect.Modifier

private const val ISSUE_CARD_TAG = "IssueCardFragment"
private const val ISSUE_CARD_ERROR_TAG = "IssueCardErrorFragment"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class IssueCardFlow (
        private val actionConfiguration: WorkflowActionConfigurationIssueCard?,
        private val cardApplicationId: String,
        val onBack: (Unit) -> Unit,
        val onFinish: (cardId: String) -> Unit
) : Flow(),
    IssueCardContract.Delegate,
    IssueCardErrorContract.Delegate {

    private val issueCardFragment: IssueCardContract.View?
        get() = fragmentWithTag(ISSUE_CARD_TAG) as? IssueCardContract.View

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        appComponent.inject(this)
        val fragment = fragmentFactory.issueCardFragment(
                uiTheme = UIConfig.uiTheme,
                cardApplicationId = cardApplicationId,
                tag = ISSUE_CARD_TAG)
        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (issueCardFragment)?.let {
            it.delegate = this
        }
        (fragmentWithTag(ISSUE_CARD_ERROR_TAG) as? IssueCardErrorContract.View)?.let {
            it.delegate = this
        }
    }

    private fun presentIssueCardError(errorCode: Int?) {
        val fragment = fragmentFactory.issueCardErrorFragment(
                UIConfig.uiTheme,
                ISSUE_CARD_ERROR_TAG,
                errorCode,
                actionConfiguration?.errorAsset)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun onRetryIssueCard() {
        popFragment()
        issueCardFragment?.issueCard()
    }

    override fun onCardIssuedFailed(errorCode: Int?) {
        presentIssueCardError(errorCode)
    }

    override fun onCardIssuedSucceeded(card: Card) {
        onFinish(card.accountID)
    }
}
