package com.aptopayments.sdk.features.card.orderphysical.success

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface OrderPhysicalCardSuccessContract {
    interface Delegate : FragmentDelegate {
        fun onBackFromPhysicalCardSuccess()
    }

    interface View {
        var delegate: Delegate?
    }
}
