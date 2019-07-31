package com.aptopayments.sdk.features.card.cardstats

import com.aptopayments.core.data.transaction.MCC
import org.threeten.bp.LocalDate

interface CardTransactionsChartContract {

    interface Delegate {
        fun onCategorySelected(mcc: MCC, startDate: LocalDate, endDate: LocalDate)
    }

    interface View {
        var delegate: Delegate?
    }
}
