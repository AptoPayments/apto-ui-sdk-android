package com.aptopayments.sdk.features.auth.verification

import com.aptopayments.mobile.data.user.DataPoint
import com.aptopayments.sdk.core.platform.FragmentDelegate
import java.io.Serializable

interface PhoneVerificationContract {

    interface Delegate : FragmentDelegate {
        fun onPhoneVerificationPassed(dataPoint: DataPoint)
        fun onBackFromPhoneVerification()
    }

    interface View {
        var delegate: Delegate?
    }
}

sealed class PINEntryState : Serializable {
    object Enabled : PINEntryState()
    object Expired : PINEntryState()
}

sealed class ResendButtonState : Serializable {
    object Enabled : ResendButtonState()
    class Waiting(val pendingSeconds: Int) : ResendButtonState()
}
