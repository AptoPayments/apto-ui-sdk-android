package com.aptopayments.sdk.features.voip

import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.voip.Action
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import java.lang.reflect.Modifier

private const val VOIP_TAG = "VoipFragment"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class VoipFlow (
        val cardId: String,
        val action: Action,
        val onBack: (Unit) -> Unit,
        val onFinish: (Unit) -> Unit
) : Flow(), VoipContract.Delegate {
    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = fragmentFactory.getVoipFragment(
                uiTheme = UIConfig.uiTheme,
                cardId = cardId,
                action = action,
                tag = VOIP_TAG)
        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(VOIP_TAG) as? VoipContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onVoipCallFinished() = onFinish(Unit)

    override fun onVoipCallError(error: String?) {
        error?.let { notify(message = it) }
    }
}
