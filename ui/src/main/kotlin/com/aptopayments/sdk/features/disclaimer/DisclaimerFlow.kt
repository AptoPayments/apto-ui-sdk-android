package com.aptopayments.sdk.features.disclaimer

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.workflowaction.WorkflowAction
import com.aptopayments.mobile.data.workflowaction.WorkflowActionConfigurationShowDisclaimer
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import org.json.JSONObject
import org.koin.core.inject

private const val DISCLAIMER_TAG = "DisclaimerFragment"

internal class DisclaimerFlow(
    private val actionConfiguration: WorkflowActionConfigurationShowDisclaimer,
    private val workflowAction: WorkflowAction,
    private val workflowObjectId: String,
    private val cardApplicationId: String,
    private val onBack: () -> Unit,
    private val onAccept: () -> Unit
) : Flow(), DisclaimerContract.Delegate {

    private val aptoPlatform: AptoPlatformProtocol by inject()
    private val analyticsManager: AnalyticsServiceContract by inject()

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val content = actionConfiguration.content
        val fragment = fragmentFactory.disclaimerFragment(content, DISCLAIMER_TAG)
        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(DISCLAIMER_TAG) as? DisclaimerContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onDisclaimerAccepted() {
        analyticsManager.track(Event.DisclaimerAccept, JSONObject().put("action_id", workflowAction.actionId))
        showLoading()
        aptoPlatform.acceptDisclaimer(workflowObjectId, workflowAction) { result ->
            hideLoading()
            result.either(::handleFailure) {
                onAccept.invoke()
            }
        }
    }

    override fun onDisclaimerRejected() {
        analyticsManager.track(Event.DisclaimerReject, JSONObject().put("action_id", workflowAction.actionId))
        aptoPlatform.cancelCardApplication(cardApplicationId) { result ->
            result.either(::handleFailure) {
                aptoPlatform.logout()
                onBack.invoke()
            }
        }
    }
}
