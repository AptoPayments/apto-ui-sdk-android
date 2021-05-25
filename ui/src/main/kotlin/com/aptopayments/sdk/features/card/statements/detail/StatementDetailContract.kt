package com.aptopayments.sdk.features.card.statements.detail

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface StatementDetailContract {

    interface Delegate : FragmentDelegate {
        fun onPdfBackPressed()
    }

    interface View {
        var delegate: Delegate?
    }
}
