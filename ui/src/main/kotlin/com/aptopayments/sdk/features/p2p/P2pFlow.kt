package com.aptopayments.sdk.features.p2p

import com.aptopayments.mobile.data.transfermoney.CardHolderData
import com.aptopayments.mobile.data.transfermoney.P2pTransferResponse
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.p2p.funds.SendFundsContract
import com.aptopayments.sdk.features.p2p.funds.SendFundsFragment
import com.aptopayments.sdk.features.p2p.recipient.P2pRecipientContract
import com.aptopayments.sdk.features.p2p.recipient.P2pRecipientFragment
import com.aptopayments.sdk.features.p2p.result.P2pResultContract
import com.aptopayments.sdk.features.p2p.result.P2pResultFragment

private const val TRANSFER_RECIPIENT_TAG = "TransferRecipientFragment"
private const val TRANSFER_SEND_FUNDS_TAG = "SendFundsFragment"
private const val TRANSFER_RESULT_FUNDS_TAG = "P2pResultFragment"

internal class P2pFlow(
    val cardId: String,
    val onFinish: () -> Unit
) : Flow(), P2pRecipientContract.Delegate, SendFundsContract.Delegate, P2pResultContract.Delegate {
    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = P2pRecipientFragment.newInstance(TRANSFER_RECIPIENT_TAG)

        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(TRANSFER_RECIPIENT_TAG) as? P2pRecipientContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(TRANSFER_SEND_FUNDS_TAG) as? SendFundsContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onTransferRecipientBackPressed() {
        onFinish()
    }

    override fun onCardholderSelected(recipient: CardHolderData) {
        val fragment = SendFundsFragment.newInstance(cardId, recipient, TRANSFER_SEND_FUNDS_TAG)
        fragment.delegate = this
        push(fragment)
    }

    override fun onPaymentSuccess(payment: P2pTransferResponse) {
        val fragment = P2pResultFragment.newInstance(payment, TRANSFER_RESULT_FUNDS_TAG)
        fragment.delegate = this
        push(fragment)
    }

    override fun onChangeRecipient() {
        popFragment()
    }

    override fun onBackFromSendFunds() {
        popFragment()
    }

    override fun onAddFundsResultsDone() {
        onFinish()
    }
}
