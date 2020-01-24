package com.aptopayments.sdk.features.card.cardstats.chart

import com.aptopayments.core.data.stats.CategorySpending
import com.aptopayments.core.data.transaction.MCC

class CategorySpendingSorter {

    fun sortByName(categorySpending: List<CategorySpending>) =
        categorySpending.sortedWith(compareBy {
            if (it.categoryId.isEmpty()) {
                null
            } else {
                MCC(name = it.categoryId, icon = MCC.Icon.valueOf(it.categoryId.toUpperCase())).toLocalizedString()
            }
        })
}
