package com.aptopayments.sdk.features.card.activatephysicalcard.success

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface ActivatePhysicalCardSuccessContract {

    interface Delegate : FragmentDelegate {
        fun onCloseFromActivatePhysicalCardSuccess()
        fun getPinFinished()
        fun onSetPinClicked()
        fun onGetPinViaVoipClicked()
    }

    interface View {
        var delegate: Delegate?
    }
}
