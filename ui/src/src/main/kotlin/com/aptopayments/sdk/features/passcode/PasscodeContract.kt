package com.aptopayments.sdk.features.passcode

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface PasscodeContract {

    interface Delegate : FragmentDelegate {
        fun onPasscodeSetCorrectly(passCode: String)
        fun onBackPressed()
    }

    interface View {
        var delegate: Delegate?
    }

}
