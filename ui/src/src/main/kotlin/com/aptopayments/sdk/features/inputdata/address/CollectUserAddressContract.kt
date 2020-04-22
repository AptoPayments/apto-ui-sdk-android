package com.aptopayments.sdk.features.inputdata.address

import com.aptopayments.core.data.user.AddressDataPoint
import com.aptopayments.sdk.core.platform.FragmentDelegate

internal interface CollectUserAddressContract {
    interface Delegate : FragmentDelegate {
        fun onAddressSelected(value: AddressDataPoint)
        fun onBackFromAddress()
    }

    interface View {
        var delegate: Delegate?
    }
}
