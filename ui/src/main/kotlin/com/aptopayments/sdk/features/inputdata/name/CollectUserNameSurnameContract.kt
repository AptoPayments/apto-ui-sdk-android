package com.aptopayments.sdk.features.inputdata.name

import com.aptopayments.mobile.data.user.NameDataPoint
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface CollectUserNameSurnameContract {
    interface Delegate : FragmentDelegate {
        fun onNameEnteredCorrectly(value: NameDataPoint)
        fun onBackFromInputName()
    }

    interface View {
        var delegate: Delegate?
    }
}
