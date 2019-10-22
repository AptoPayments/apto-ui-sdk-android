package com.aptopayments.sdk.features.card.statements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.statements.MonthlyStatement
import com.aptopayments.core.data.statements.MonthlyStatementPeriod
import com.aptopayments.core.data.statements.StatementMonth
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.data.StatementFile
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.repository.StatementRepository
import kotlinx.coroutines.launch

internal class StatementListViewModel(
    private val analyticsManager: AnalyticsServiceContract,
    private val statementRepository: StatementRepository
) : BaseViewModel() {

    private val _statementList = MutableLiveData(listOf<StatementListItem>())
    val statementList: LiveData<List<StatementListItem>>
        get() = _statementList

    private val _statementListEmpty = MutableLiveData<Boolean>()
    val statementListEmpty: LiveData<Boolean>
        get() = _statementListEmpty

    private val _file = MutableLiveData<StatementFile>()
    val file: LiveData<StatementFile>
        get() = _file

    fun viewLoaded() {
        analyticsManager.track(Event.MonthlyStatementsListStart)
    }

    fun fetchStatementList() {
        AptoPlatform.fetchMonthlyStatementPeriod { result ->
            result.either(::handleFailure, ::handleStatementListSuccess)
        }
    }

    fun onMonthTapped(monthStatement: StatementMonth) {
        AptoPlatform.fetchMonthlyStatement(monthStatement.month, monthStatement.year) { result ->
            result.either(::handleFailure, ::handleStatementGetSuccess)
        }
    }

    private fun handleStatementListSuccess(period: MonthlyStatementPeriod) {
        val generator = StatementListGenerator()
        if (period.isValid()) {
            _statementList.postValue(generator.generate(period))
        }
        _statementListEmpty.postValue(!period.isValid())
    }

    private fun handleStatementGetSuccess(monthlyStatement: MonthlyStatement) {
        viewModelScope.launch {
            statementRepository
                .download(monthlyStatement)
                .either(::handleFailure) {
                    _file.postValue(it)
                }
        }
    }
}
