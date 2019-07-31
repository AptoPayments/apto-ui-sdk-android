package com.aptopayments.sdk.features.card.cardstats

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.data.stats.CategorySpending
import com.aptopayments.core.data.stats.MonthlySpending
import com.aptopayments.core.data.transaction.MCC
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.core.analytics.Event
import javax.inject.Inject

internal class CardMonthlyStatsViewModel @Inject constructor(
        private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {
    var monthlySpending: MutableLiveData<MonthlySpending> = MutableLiveData()
    var monthlySpendingMap: MutableLiveData<HashMap<Pair<String, String>, List<CategorySpending>>> = MutableLiveData()

    fun getMonthlySpending(context: Context, cardId: String, month: String, year: String, onComplete: (() -> Unit)? = null) {
        AptoPlatform.cardMonthlySpending(cardId, month, year) { result ->
            val spendingMap = monthlySpendingMap.value ?: HashMap()
            result.either(::handleFailure) {
                monthlySpending.postValue(it)
                val sortedList = sortCategorySpending(context, it.spending)
                spendingMap[Pair(month, year)] = sortedList
                monthlySpendingMap.postValue(spendingMap)
                onComplete?.invoke()
                Unit
            }
        }
    }

    private fun sortCategorySpending(context: Context, categorySpending: List<CategorySpending>) =
            categorySpending.sortedWith(compareBy {
                val mcc = if (it.categoryId.isEmpty()) null
                else MCC(name = it.categoryId, icon = MCC.Icon.valueOf(it.categoryId.toUpperCase()))
                mcc?.toString(context)
            })

    fun viewLoaded() = analyticsManager.track(Event.MonthlySpending)

    fun invalidateCache() = AptoPlatform.clearMonthlySpendingCache()
}
