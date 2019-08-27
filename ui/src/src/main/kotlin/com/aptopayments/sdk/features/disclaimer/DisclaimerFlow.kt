package com.aptopayments.sdk.features.disclaimer

import androidx.annotation.VisibleForTesting
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.workflowaction.WorkflowAction
import com.aptopayments.core.data.workflowaction.WorkflowActionConfigurationShowDisclaimer
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.core.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import org.json.JSONObject
import org.koin.core.inject
import java.lang.reflect.Modifier

private const val DISCLAIMER_TAG = "DisclaimerFragment"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class DisclaimerFlow (
        private var actionConfiguration: WorkflowActionConfigurationShowDisclaimer,
        private var workflowAction: WorkflowAction,
        private var workflowObjectId: String,
        private var cardApplicationId: String,
        var onBack: (Unit) -> Unit,
        var onAccept: (Unit) -> Unit
) : Flow(), DisclaimerContract.Delegate {

    private val aptoPlatform: AptoPlatformProtocol by inject()
    private val analyticsManager: AnalyticsServiceContract by inject()

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val content = actionConfiguration.content
        val fragment = fragmentFactory.disclaimerFragment(
                UIConfig.uiTheme,
                content,
                DISCLAIMER_TAG)
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
                onAccept(Unit)
            }
        }
    }

    override fun onDisclaimerRejected() {
        analyticsManager.track(Event.DisclaimerReject,  JSONObject().put("action_id", workflowAction.actionId))
        aptoPlatform.cancelCardApplication(cardApplicationId) { result ->
            result.either(::handleFailure) {
                aptoPlatform.logout()
                onBack(Unit)
            }
        }
    }
}
