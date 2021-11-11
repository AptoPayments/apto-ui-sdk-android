package com.aptopayments.sdk.features.p2p.funds

import com.aptopayments.mobile.data.transfermoney.P2pTransferResponse
import com.aptopayments.sdk.core.platform.FragmentDelegate

internal interface SendFundsContract {

    interface Delegate : FragmentDelegate {
        fun onPaymentSuccess(payment: P2pTransferResponse)
        fun onChangeRecipient()
        fun onBackFromSendFunds()
    }

    interface View {
        var delegate: Delegate?
    }
}
