package com.aptopayments.sdk.features.card.cardstats

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
import org.threeten.bp.LocalDate

internal class CardMonthlyStatsViewModel constructor(
    private val analyticsManager: AnalyticsServiceContract,
    private val statementRepository: StatementRepository
) : BaseViewModel() {
    var monthlySpendingMap: MutableLiveData<HashMap<Pair<String, String>, List<CategorySpending>>> = MutableLiveData()

    fun getMonthlySpending(
        cardId: String, month: String, year: String,
        onComplete: ((monthlySpending: MonthlySpending) -> Unit)? = null
    ) {
        AptoPlatform.cardMonthlySpending(cardId, month, year) { result ->
            val spendingMap = monthlySpendingMap.value ?: HashMap()
            result.either(::handleFailure) {
                val sortedList = sortCategorySpending(it.spending)
                spendingMap[Pair(month, year)] = sortedList
                monthlySpendingMap.postValue(spendingMap)
                onComplete?.invoke(it)
                Unit
            }
        }
    }

    private fun sortCategorySpending(categorySpending: List<CategorySpending>) =
        categorySpending.sortedWith(compareBy {
            val mcc = if (it.categoryId.isEmpty()) null
            else MCC(name = it.categoryId, icon = MCC.Icon.valueOf(it.categoryId.toUpperCase()))
            mcc?.toString()
        })

    fun viewLoaded() = analyticsManager.track(Event.MonthlySpending)

    fun invalidateCache() = AptoPlatform.clearMonthlySpendingCache()

    fun hasMonthlyStatementToShow(date: LocalDate, onComplete: ((included: Boolean) -> Unit)) {
        if (AptoPlatform.cardOptions.showMonthlyStatementOption() && (date != LocalDate.MAX)) {
            getMonthlyStatementPeriod(date.monthValue, date.year, onComplete)
        } else {
            onComplete(false)
        }
    }

    fun getMonthlyStatement(month: Int, year: Int, onComplete: ((statementFile: StatementFile) -> Unit)) {
        AptoPlatform.fetchMonthlyStatement(month, year) { result ->
            result.either(::handleFailure) {
                viewModelScope.launch {
                    statementRepository.download(it).either(::handleFailure) { onComplete(it) }
                }
            }
        }
    }

    private fun getMonthlyStatementPeriod(month: Int, year: Int, onComplete: (included: Boolean) -> Unit) {
        AptoPlatform.fetchMonthlyStatementPeriod { result ->
            result.either(::handleFailure) { period -> onStatementPeriodArrived(period, month, year, onComplete) }
        }
    }

    private fun onStatementPeriodArrived(
        period: MonthlyStatementPeriod,
        month: Int,
        year: Int,
        onComplete: (included: Boolean) -> Unit
    ) = onComplete.invoke(period.contains(month, year))

}
