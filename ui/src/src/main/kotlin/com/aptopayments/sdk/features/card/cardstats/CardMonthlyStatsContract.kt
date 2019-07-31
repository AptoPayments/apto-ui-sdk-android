package com.aptopayments.sdk.features.card.cardstats

import com.aptopayments.core.data.transaction.MCC
import com.aptopayments.sdk.core.platform.FragmentDelegate
import org.threeten.bp.LocalDate

interface CardMonthlyStatsContract {

    interface Delegate : FragmentDelegate {
        fun onBackFromCardMonthlyStats()
        fun onCategorySelected(mcc: MCC, startDate: LocalDate, endDate: LocalDate)
    }

    interface View {
        var delegate: Delegate?
    }
}
