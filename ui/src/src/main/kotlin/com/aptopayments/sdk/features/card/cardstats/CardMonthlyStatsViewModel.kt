package com.aptopayments.sdk.features.card.cardstats

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.statements.MonthlyStatementPeriod
import com.aptopayments.core.data.stats.CategorySpending
import com.aptopayments.core.data.stats.MonthlySpending
import com.aptopayments.core.data.transaction.MCC
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.data.StatementFile
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.repository.StatementRepository
import kotlinx.coroutines.launch

internal class CardMonthlyStatsViewModel constructor(
    private val analyticsManager: AnalyticsServiceContract,
    private val statementRepository: StatementRepository
) : BaseViewModel() {
    private var periodCache: MonthlyStatementPeriod? = null
    var monthlySpending: MutableLiveData<MonthlySpending> = MutableLiveData()
    var monthlySpendingMap: MutableLiveData<HashMap<Pair<String, String>, List<CategorySpending>>> = MutableLiveData()

    fun getMonthlySpending(context: Context, cardId: String, month: String, year: String,
                           onComplete: ((monthlySpending: MonthlySpending) -> Unit)? = null) {
        AptoPlatform.cardMonthlySpending(cardId, month, year) { result ->
            val spendingMap = monthlySpendingMap.value ?: HashMap()
            result.either(::handleFailure) {
                monthlySpending.postValue(it)
                val sortedList = sortCategorySpending(context, it.spending)
                spendingMap[Pair(month, year)] = sortedList
                monthlySpendingMap.postValue(spendingMap)
                onComplete?.invoke(it)
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

    fun hasMonthlyStatementToShow(month: Int, year: Int, onComplete: ((included: Boolean) -> Unit)) {
        if (periodCache != null) {
            completeStatementPeriodRequest(month, year, onComplete)
        } else {
            AptoPlatform.fetchMonthlyStatementPeriod { result ->
                result.either(::handleFailure) { period -> onStatementPeriodArrived(period, month, year, onComplete) }
            }
        }
    }

    fun getMonthlyStatement(month: Int, year: Int, onComplete: ((statementFile: StatementFile) -> Unit)) {
        AptoPlatform.fetchMonthlyStatement(month, year) { result ->
            result.either(::handleFailure) {
                viewModelScope.launch {
                    statementRepository.download(it).either(::handleFailure) {
                        onComplete(it)
                    }
                }
            }
        }
    }

    private fun onStatementPeriodArrived(
        period: MonthlyStatementPeriod,
        month: Int,
        year: Int,
        onComplete: (included: Boolean) -> Unit
    ) {
        periodCache = period
        completeStatementPeriodRequest(month, year, onComplete)
    }

    private fun completeStatementPeriodRequest(month: Int, year: Int, onComplete: (included: Boolean) -> Unit) {
        onComplete.invoke(periodCache!!.contains(month, year))
    }
}
