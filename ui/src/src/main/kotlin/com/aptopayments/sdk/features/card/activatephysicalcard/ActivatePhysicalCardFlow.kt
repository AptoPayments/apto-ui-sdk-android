package com.aptopayments.sdk.features.card.activatephysicalcard

import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.voip.Action
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.extension.localized
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.card.activatephysicalcard.activate.ActivatePhysicalCardContract
import com.aptopayments.sdk.features.card.activatephysicalcard.success.ActivatePhysicalCardSuccessContract
import com.aptopayments.sdk.features.card.setpin.SetPinFlow
import com.aptopayments.sdk.features.voip.VoipFlow
import com.aptopayments.sdk.utils.MessageBanner
import java.lang.reflect.Modifier

private const val ACTIVATE_PHYSICAL_CARD_TAG = "ActivatePhysicalCardFragment"
private const val ACTIVATE_PHYSICAL_CARD_SUCCESS_TAG = "ActivatePhysicalCardSuccessFragment"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class ActivatePhysicalCardFlow (
        var card: Card,
        var onBack: (Unit) -> Unit,
        var onFinish: (Unit) -> Unit,
        var onPhysicalCardActivated: (Unit) -> Unit
) : Flow(),
        ActivatePhysicalCardContract.Delegate,
        ActivatePhysicalCardSuccessContract.Delegate
{

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        appComponent.inject(this)
        val fragment = fragmentFactory.activatePhysicalCardFragment(
                uiTheme = UIConfig.uiTheme,
                card = card,
                tag = ACTIVATE_PHYSICAL_CARD_TAG)
        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(ACTIVATE_PHYSICAL_CARD_TAG) as? ActivatePhysicalCardContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(ACTIVATE_PHYSICAL_CARD_SUCCESS_TAG) as? ActivatePhysicalCardSuccessContract.View)?.let {
            it.delegate = this
        }
    }

    //
    // Activate
    //
    override fun onPhysicalCardActivated() {
        val fragment = fragmentFactory.activatePhysicalCardSuccessFragment(
                uiTheme = UIConfig.uiTheme,
                card = card,
                tag = ACTIVATE_PHYSICAL_CARD_SUCCESS_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
        onPhysicalCardActivated(Unit)
    }

    override fun onBackFromActivatePhysicalCard() = onBack(Unit)

    //
    // Success
    //
    override fun getPinFinished() = onFinish(Unit)

    override fun onCloseFromActivatePhysicalCardSuccess() = onFinish(Unit)

    override fun onSetPinClicked() {
        val flow = SetPinFlow(
                cardId = card.accountID,
                onBack = { popFlow(animated = true) },
                onFinish = {
                    popFlow(animated = true)
                    rootActivity()?.let {
                        notify(title = "manage_card.confirm_pin.pin_created.title".localized(it),
                                message = "manage_card.confirm_pin.pin_created.message".localized(it),
                                messageType = MessageBanner.MessageType.HEADS_UP)
                    }
                    onFinish(Unit)
                }
        )
        flow.init { initResult ->
            initResult.either(::handleFailure) { push(flow = flow) }
        }
    }

    override fun onGetPinViaVoipClicked() {
        val flow = VoipFlow(
                cardId = card.accountID,
                action = Action.LISTEN_PIN,
                onBack = { popFlow(animated = true) },
                onFinish = {
                    popFlow(animated = true)
                    onFinish(Unit)
                }
        )
        flow.init { initResult ->
            initResult.either(::handleFailure) { push(flow = flow) }
        }
    }
}
