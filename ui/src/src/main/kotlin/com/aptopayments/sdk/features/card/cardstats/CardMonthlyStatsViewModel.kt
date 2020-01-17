package com.aptopayments.sdk.features.card.cardstats

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.statements.MonthlyStatement
import com.aptopayments.core.data.statements.MonthlyStatementPeriod
import com.aptopayments.core.data.stats.CategorySpending
import com.aptopayments.core.data.stats.MonthlySpending
import com.aptopayments.core.data.transaction.MCC
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.core.usecase.DownloadStatementUseCase
import com.aptopayments.sdk.data.StatementFile
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.threeten.bp.LocalDate

internal class CardMonthlyStatsViewModel constructor(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel(), KoinComponent {

    private val downloadUseCase: DownloadStatementUseCase by inject()

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
            mcc?.toLocalizedString()
        })

    fun viewLoaded() = analyticsManager.track(Event.MonthlySpending)

    fun invalidateCache() = AptoPlatform.clearMonthlySpendingCache()

    fun hasMonthlyStatementToShow(date: LocalDate, onComplete: ((included: Boolean) -> Unit)) {
        if (AptoUiSdk.cardOptions.showMonthlyStatementOption() && (date != LocalDate.MAX)) {
            getMonthlyStatementPeriod(date.monthValue, date.year, onComplete)
        } else {
            onComplete(false)
        }
    }

    fun getMonthlyStatement(month: Int, year: Int, onComplete: ((statementFile: StatementFile) -> Unit)) {
        AptoPlatform.fetchMonthlyStatement(month, year) { result ->
            result.either(::handleFailure) {
                downloadStatement(it, onComplete)
            }
        }
    }

    private fun downloadStatement(it: MonthlyStatement, onComplete: (statementFile: StatementFile) -> Unit) {
        viewModelScope.launch {
            val params = DownloadStatementUseCase.Params(it)
            downloadUseCase(params).either(::handleFailure) { onComplete(it) }
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
