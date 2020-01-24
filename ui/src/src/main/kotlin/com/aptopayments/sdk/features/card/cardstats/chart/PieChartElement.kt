package com.aptopayments.sdk.features.card.cardstats.chart

import com.aptopayments.core.data.stats.CategorySpending
import com.aptopayments.core.data.transaction.MCC

data class PieChartElement(
        val categorySpending: CategorySpending,
        val mcc: MCC? = null
)
