package com.aptopayments.sdk.features.card.statements

import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.sdk.core.platform.FragmentDelegate

internal interface StatementListContract {

    interface Delegate : FragmentDelegate {
        fun onBackPressed()
        fun onStatementPressed(statementMonth: StatementMonth)
    }

    interface View {
        var delegate: Delegate?
    }
}
