package com.aptopayments.sdk.features.card.cardstats

import com.aptopayments.mobile.data.transaction.MCC
import com.aptopayments.sdk.core.platform.FragmentDelegate
import com.aptopayments.sdk.data.StatementFile
import org.threeten.bp.LocalDate

interface CardMonthlyStatsContract {

    interface Delegate : FragmentDelegate {
        fun onBackFromCardMonthlyStats()
        fun onCategorySelected(mcc: MCC, startDate: LocalDate, endDate: LocalDate)
        fun showMonthlyStatement(file: StatementFile)
    }

    interface View {
        var delegate: Delegate?
    }
}
