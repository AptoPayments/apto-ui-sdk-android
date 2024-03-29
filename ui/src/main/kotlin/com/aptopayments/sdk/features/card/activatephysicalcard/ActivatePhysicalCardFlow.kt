package com.aptopayments.sdk.features.card.activatephysicalcard

import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.voip.Action
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.card.activatephysicalcard.activate.ActivatePhysicalCardContract
import com.aptopayments.sdk.features.card.activatephysicalcard.success.ActivatePhysicalCardSuccessContract
import com.aptopayments.sdk.features.voip.VoipFlow
import com.aptopayments.sdk.repository.CardActionRepository
import org.koin.core.inject

private const val ACTIVATE_PHYSICAL_CARD_TAG = "ActivatePhysicalCardFragment"
private const val ACTIVATE_PHYSICAL_CARD_SUCCESS_TAG = "ActivatePhysicalCardSuccessFragment"

internal class ActivatePhysicalCardFlow(
    val card: Card,
    val onBack: () -> Unit,
    val onFinish: () -> Unit,
    val onPhysicalCardActivated: () -> Unit
) : Flow(), ActivatePhysicalCardContract.Delegate, ActivatePhysicalCardSuccessContract.Delegate {

    val cardActionRepo: CardActionRepository by inject()

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = fragmentFactory.activatePhysicalCardFragment(card, ACTIVATE_PHYSICAL_CARD_TAG)
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
        val fragment = fragmentFactory.activatePhysicalCardSuccessFragment(card, ACTIVATE_PHYSICAL_CARD_SUCCESS_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
        onPhysicalCardActivated.invoke()
    }

    override fun onBackFromActivatePhysicalCard() = onBack.invoke()

    //
    // Success
    //
    override fun getPinFinished() = onFinish.invoke()

    override fun onCloseFromActivatePhysicalCardSuccess() = onFinish.invoke()

    override fun onSetPinClicked() {
        cardActionRepo.setPin()
        onFinish.invoke()
    }

    override fun onGetPinViaVoipClicked() {
        val flow = VoipFlow(
            cardId = card.accountID,
            action = Action.LISTEN_PIN,
            onBack = { popFlow(animated = true) },
            onFinish = {
                popFlow(animated = true)
                onFinish.invoke()
            }
        )
        flow.init { initResult ->
            initResult.either(::handleFailure) { push(flow = flow) }
        }
    }
}
