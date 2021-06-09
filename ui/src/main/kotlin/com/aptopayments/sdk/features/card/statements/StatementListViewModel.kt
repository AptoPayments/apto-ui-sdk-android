package com.aptopayments.sdk.features.card.statements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.statements.MonthlyStatementPeriod
import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent
import org.koin.core.KoinComponent

internal class StatementListViewModel(
    analyticsManager: AnalyticsServiceContract,
    private val aptoPlatform: AptoPlatformProtocol
) : BaseViewModel(), KoinComponent {

    private val _state = MutableLiveData(State())
    val state = _state as LiveData<State>

    val action = LiveEvent<Action>()

    init {
        analyticsManager.track(Event.MonthlyStatementsListStart)
        fetchStatementList()
    }

    private fun fetchStatementList() {
        showLoading()
        aptoPlatform.fetchMonthlyStatementPeriod { result ->
            hideLoading()
            result.either(::handleFailure, ::handleStatementListSuccess)
        }
    }

    fun onMonthTapped(monthStatement: StatementMonth) {
        action.postValue(Action.OpenMonth(monthStatement))
    }

    private fun handleStatementListSuccess(period: MonthlyStatementPeriod) {
        hideLoading()
        val generator = StatementListGenerator()
        if (period.isValid()) {
            _state.postValue(State(showEmpty = false, list = generator.generate(period)))
        } else {
            _state.postValue(State(showEmpty = true, list = generator.generate(period)))
        }
    }

    sealed class Action {
        class OpenMonth(val statementMonth: StatementMonth) : Action()
    }

    data class State(
        val showEmpty: Boolean = false,
        val list: List<StatementListItem> = emptyList()
    )
}
