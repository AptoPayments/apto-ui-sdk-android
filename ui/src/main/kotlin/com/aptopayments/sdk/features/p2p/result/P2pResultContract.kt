package com.aptopayments.sdk.features.p2p.result

import com.aptopayments.sdk.core.platform.FragmentDelegate

internal interface P2pResultContract {

    interface Delegate : FragmentDelegate {
        fun onAddFundsResultsDone()
    }

    interface View {
        var delegate: Delegate?
    }
}
