package com.aptopayments.sdk.features.card.statements

import com.aptopayments.sdk.core.platform.FragmentDelegate
import com.aptopayments.sdk.data.StatementFile

internal interface StatementListContract {

    interface Delegate : FragmentDelegate {
        fun onBackPressed()
        fun onStatementDownloaded(file: StatementFile)
    }

    interface View {
        var delegate: Delegate?
    }
}
