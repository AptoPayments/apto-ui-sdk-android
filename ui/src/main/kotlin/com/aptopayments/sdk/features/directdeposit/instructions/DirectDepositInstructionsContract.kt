package com.aptopayments.sdk.features.directdeposit.instructions

import com.aptopayments.sdk.core.platform.FragmentDelegate

internal interface DirectDepositInstructionsContract {

    interface Delegate : FragmentDelegate {
        fun onBackFromDirectDepositInstructions()
    }

    interface View {
        var delegate: Delegate?
    }
}
