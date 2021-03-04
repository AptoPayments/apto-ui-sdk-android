package com.aptopayments.sdk.features.loadfunds.dialog

import com.aptopayments.sdk.core.platform.FragmentDelegate

internal interface AddFundsSelectorDialogContract {

    interface Delegate : FragmentDelegate {
        fun addFundsSelectorCardClicked()
        fun addFundsSelectorAchClicked()
    }

    interface View {
        var delegate: Delegate?
    }
}
