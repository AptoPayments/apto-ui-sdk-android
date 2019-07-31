package com.aptopayments.core.data.stats

import com.aptopayments.core.data.transaction.MCC

data class PieChartElement(
        val categorySpending: CategorySpending,
        val mcc: MCC? = null
)
