package com.aptopayments.sdk.features.card.orderphysical.initial

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface OrderPhysicalCardContract {
    interface Delegate : FragmentDelegate {
        fun onBackFromPhysicalCardOrder()
        fun onCardOrdered()
    }

    interface View {
        var delegate: Delegate?
    }
}
