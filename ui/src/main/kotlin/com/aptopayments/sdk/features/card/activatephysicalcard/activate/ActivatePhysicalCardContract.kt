package com.aptopayments.sdk.features.card.activatephysicalcard.activate

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface ActivatePhysicalCardContract {

    interface Delegate : FragmentDelegate {
        fun onPhysicalCardActivated()
        fun onBackFromActivatePhysicalCard()
    }

    interface View {
        var delegate: Delegate?
    }
}
