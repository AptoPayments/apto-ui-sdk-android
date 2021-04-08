package com.aptopayments.sdk.features.card.orderphysical

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.card.orderphysical.initial.OrderPhysicalCardContract
import com.aptopayments.sdk.features.card.orderphysical.success.OrderPhysicalCardSuccessContract

private const val ORDER_PHYSICAL_CARD_TAG = "OrderPhysicalCardFragment"
private const val ORDER_PHYSICAL_CARD_SUCCESS_TAG = "OrderPhysicalCardSuccessFragment"

internal class OrderPhysicalFlow(
    val cardId: String,
    var onClose: () -> Unit
) : Flow(), OrderPhysicalCardContract.Delegate, OrderPhysicalCardSuccessContract.Delegate {

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = fragmentFactory.orderPhysicalCardFragment(cardId, ORDER_PHYSICAL_CARD_TAG)
        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentDialogWithTag(ORDER_PHYSICAL_CARD_TAG) as? OrderPhysicalCardContract.View)?.let {
            it.delegate = this
        }
        (fragmentDialogWithTag(ORDER_PHYSICAL_CARD_SUCCESS_TAG) as? OrderPhysicalCardSuccessContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onBackFromPhysicalCardOrder() {
        onClose()
    }

    override fun onCardOrdered() {
        val fragment =
            fragmentFactory.orderPhysicalCardSuccessFragment(cardId = cardId, tag = ORDER_PHYSICAL_CARD_SUCCESS_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun onBackFromPhysicalCardSuccess() {
        onClose()
    }
}
