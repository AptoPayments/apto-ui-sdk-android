package com.aptopayments.sdk.features.card.cardstats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.statements.MonthlyStatement
import com.aptopayments.mobile.data.stats.MonthlySpending
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.extension.monthLocalized
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.core.usecase.DownloadStatementUseCase
import com.aptopayments.sdk.data.StatementFile
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.DateProvider
import com.aptopayments.sdk.utils.LiveEvent
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.threeten.bp.LocalDate

private const val EMPTY = ""

internal class CardMonthlyStatsViewModel(
    private val cardId: String,
    private val aptoPlatformProtocol: AptoPlatformProtocol,
    private val analyticsManager: AnalyticsServiceContract,
    dateProvider: DateProvider
) : BaseViewModel(), KoinComponent {

    private val downloadUseCase: DownloadStatementUseCase by inject()

    private val now = dateProvider.localDate()
    private var currentMonth = LocalDate.of(now.year, now.month, 1)
    private val _previousMonthName = MutableLiveData(EMPTY)
    private val _currentMonthName = MutableLiveData(currentMonth.monthLocalized())
    private val _nextMonthName = MutableLiveData(EMPTY)
    val previousMonthName = _previousMonthName as LiveData<String>
    val currentMonthName = _currentMonthName as LiveData<String>
    val nextMonthName = _nextMonthName as LiveData<String>
    val addSpending: LiveEvent<LocalDate> = LiveEvent()

    init {
        showLoading()
        analyticsManager.track(Event.MonthlySpending)
    }

    fun onMonthSelected(date: LocalDate) {
        changeMonthsNameBeforeCall(date, currentMonth)
        currentMonth = date

        getMonthlySpending(date) { result ->
            result.either(::handleFailure) { spending ->
                changeMonthsNameAfterCall(date, spending)
                configurePreviousMonth(spending, date)
                hideLoading()
            }
        }
    }

    private fun getMonthlySpending(date: LocalDate, callback: (Either<Failure, MonthlySpending>) -> Unit) =
        aptoPlatformProtocol.cardMonthlySpending(cardId, date.monthValue, date.year, callback)

    private fun configurePreviousMonth(spending: MonthlySpending, date: LocalDate) {
        if (spending.prevSpendingExists) {
            addPreviousMonth(date)
            prefetchPreviousSpending(date)
        }
    }

    private fun addPreviousMonth(date: LocalDate) = addSpending.postValue(date.minusMonths(1))

    private fun prefetchPreviousSpending(date: LocalDate) = getMonthlySpending(date.minusMonths(1)) {}

    private fun changeMonthsNameBeforeCall(newDate: LocalDate, oldDate: LocalDate) {
        if (!newDate.isEqual(oldDate)) {
            if (newDate.isBefore(oldDate)) {
                _previousMonthName.value = EMPTY
                configureMonthName(_nextMonthName, oldDate)
            } else {
                configureMonthName(_previousMonthName, oldDate)
                _nextMonthName.value = EMPTY
            }
        }
        configureMonthName(_currentMonthName, newDate, 0)
    }

    private fun changeMonthsNameAfterCall(date: LocalDate, spending: MonthlySpending) {
        configureMonthName(_previousMonthName, date, -1, spending.prevSpendingExists)
        configureMonthName(_nextMonthName, date, 1, spending.nextSpendingExists)
    }

    private fun configureMonthName(
        liveData: MutableLiveData<String>,
        date: LocalDate,
        monthAddition: Long = 0,
        condition: Boolean = true
    ) {
        liveData.value = if (condition) {
            date.plusMonths(monthAddition).monthLocalized()
        } else {
            EMPTY
        }
    }

    override fun onCleared() {
        super.onCleared()
        invalidateCache()
    }

    fun getMonthlyStatement(month: Int, year: Int, onComplete: ((statementFile: StatementFile) -> Unit)) {
        aptoPlatformProtocol.fetchMonthlyStatement(month, year) { result ->
            result.either(::handleFailure) {
                downloadStatement(it, onComplete)
            }
        }
    }

    private fun downloadStatement(statement: MonthlyStatement, onComplete: (statementFile: StatementFile) -> Unit) {
        viewModelScope.launch {
            val params = DownloadStatementUseCase.Params(statement)
            downloadUseCase(params).either(::handleFailure) { onComplete(it) }
        }
    }

    private fun invalidateCache() = aptoPlatformProtocol.clearMonthlySpendingCache()
}
