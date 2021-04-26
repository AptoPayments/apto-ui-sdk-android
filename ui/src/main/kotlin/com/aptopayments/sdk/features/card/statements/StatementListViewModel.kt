package com.aptopayments.sdk.features.card.statements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.statements.MonthlyStatement
import com.aptopayments.mobile.data.statements.MonthlyStatementPeriod
import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.core.usecase.DownloadStatementUseCase
import com.aptopayments.sdk.data.StatementFile
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

internal class StatementListViewModel(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel(), KoinComponent {

    private val _statementList = MutableLiveData(listOf<StatementListItem>())
    val statementList = _statementList as LiveData<List<StatementListItem>>

    val statementListEmpty = LiveEvent<Boolean>()

    private val _file = MutableLiveData<StatementFile>()
    val file = _file as LiveData<StatementFile>

    private val downloadUseCase: DownloadStatementUseCase by inject()

    init {
        analyticsManager.track(Event.MonthlyStatementsListStart)
    }

    fun fetchStatementList() {
        viewModelScope.launch {
            showLoading()
            AptoPlatform.fetchMonthlyStatementPeriod { result ->
                result.either(::handleFailure, ::handleStatementListSuccess)
            }
        }
    }

    fun onMonthTapped(monthStatement: StatementMonth) {
        AptoPlatform.fetchMonthlyStatement(monthStatement.month, monthStatement.year) {
            it.either(::handleFailure, ::handleStatementGetSuccess)
        }
    }

    private fun handleStatementListSuccess(period: MonthlyStatementPeriod) {
        hideLoading()
        val generator = StatementListGenerator()
        if (period.isValid()) {
            _statementList.postValue(generator.generate(period))
        }
        statementListEmpty.postValue(!period.isValid())
    }

    private fun handleStatementGetSuccess(monthlyStatement: MonthlyStatement) {
        viewModelScope.launch {
            downloadUseCase.run(DownloadStatementUseCase.Params(monthlyStatement))
                .either(::handleFailure) {
                    _file.postValue(it)
                }
        }
    }
}
