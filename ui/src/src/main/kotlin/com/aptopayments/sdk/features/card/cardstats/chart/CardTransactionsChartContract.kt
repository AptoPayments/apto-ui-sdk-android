package com.aptopayments.sdk.features.card.cardstats.chart

import com.aptopayments.core.data.transaction.MCC
import org.threeten.bp.LocalDate

interface CardTransactionsChartContract {

    interface Delegate {
        fun onCategorySelected(mcc: MCC, startDate: LocalDate, endDate: LocalDate)
        fun onStatementTapped(month: Int, year: Int)
    }

    interface View {
        var delegate: Delegate?
    }
}
