package com.aptopayments.sdk.features.card.cardstats.chart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.aptopayments.mobile.data.stats.MonthlySpending
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.core.platform.BaseViewModel
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.threeten.bp.LocalDate

internal class CardTransactionsChartViewModel(
    private val cardId: String,
    private val date: LocalDate,
    private val aptoPlatform: AptoPlatformProtocol
) : BaseViewModel(), KoinComponent {

    private val categorySpendingSorter: CategorySpendingSorter by inject()

    private val _spending = MutableLiveData<MonthlySpending>(MonthlySpending(false, false, listOf()))
    private val _hasMonthlyStatement = MutableLiveData(false)
    val categorySpending = Transformations.map(_spending) { categorySpendingSorter.sortByName(it.spending) }
    val hasMonthlyStatement = _hasMonthlyStatement as LiveData<Boolean>

    init {
        getMonthlySpending()
        configureMonthlyStatement()
    }

    private fun getMonthlySpending() {
        aptoPlatform.cardMonthlySpending(cardId, date.monthValue, date.year) { result ->
            result.either(::handleFailure) {
                _spending.postValue(it)
            }
        }
    }

    private fun configureMonthlyStatement() {
        if (AptoUiSdk.cardOptions.showMonthlyStatementOption()) {
            getMonthlyStatementPeriod(date.monthValue, date.year)
        } else {
            _hasMonthlyStatement.postValue(false)
        }
    }

    private fun getMonthlyStatementPeriod(month: Int, year: Int) {
        aptoPlatform.fetchMonthlyStatementPeriod { result ->
            result.either(::handleFailure) { period -> _hasMonthlyStatement.postValue(period.contains(month, year)) }
        }
    }
}
