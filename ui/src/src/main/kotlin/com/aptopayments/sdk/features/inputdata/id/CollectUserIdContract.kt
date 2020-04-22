package com.aptopayments.sdk.features.inputdata.id

import com.aptopayments.core.data.user.IdDocumentDataPoint
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface CollectUserIdContract {
    interface Delegate : FragmentDelegate {
        fun onIdEnteredCorrectly(value: IdDocumentDataPoint)
        fun onBackFromCollectId()
    }

    interface View {
        var delegate: Delegate?
    }
}
