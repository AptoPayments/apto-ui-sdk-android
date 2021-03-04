package com.aptopayments.sdk.features.directdeposit.details

import com.aptopayments.sdk.core.platform.FragmentDelegate

internal interface AchAccountDetailsDialogContract {

    interface Delegate : FragmentDelegate

    interface View {
        var delegate: Delegate?
    }
}
