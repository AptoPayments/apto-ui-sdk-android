package com.aptopayments.sdk.features.card.cardstats.chart

import com.aptopayments.mobile.data.stats.CategorySpending
import com.aptopayments.mobile.data.transaction.MCC

data class PieChartElement(
    val categorySpending: CategorySpending,
    val mcc: MCC? = null
)
