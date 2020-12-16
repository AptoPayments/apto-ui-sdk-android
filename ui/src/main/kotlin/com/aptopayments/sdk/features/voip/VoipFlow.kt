package com.aptopayments.sdk.features.voip

import com.aptopayments.mobile.data.voip.Action
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable

private const val VOIP_TAG = "VoipFragment"

internal class VoipFlow(
    val cardId: String,
    val action: Action,
    val onBack: () -> Unit,
    val onFinish: () -> Unit
) : Flow(), VoipContract.Delegate {
    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = fragmentFactory.getVoipFragment(cardId, action, VOIP_TAG)
        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(VOIP_TAG) as? VoipContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onVoipCallFinished() = onFinish.invoke()

    override fun onVoipCallError(error: String?) {
        error?.let { notify(message = it) }
    }
}
