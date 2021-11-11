package com.aptopayments.sdk.features.p2p.result

import com.aptopayments.mobile.data.payment.PaymentStatus
import com.aptopayments.mobile.data.transfermoney.P2pTransferResponse
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.utils.LiveEvent
import com.aptopayments.sdk.utils.extensions.formatForTransactionDetails

class P2pResultViewModel(
    result: P2pTransferResponse
) : BaseViewModel() {

    val action = LiveEvent<Action>()

    val state = with(result) {
        State(
            status = status,
            name = recipientName.toString(),
            amount = amount.toString(),
            time = this.createdAt.formatForTransactionDetails()
        )
    }

    fun onCtaClicked() {
        action.postValue(Action.CtaClicked)
    }

    sealed class Action {
        object CtaClicked : Action()
    }

    data class State(
        val status: PaymentStatus = PaymentStatus.PROCESSED,
        val name: String = "",
        val amount: String = "",
        val time: String = "",
    )
}
