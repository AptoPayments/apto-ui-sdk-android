package com.aptopayments.sdk.features.card.statements.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.core.usecase.DownloadStatementExternalUseCase
import com.aptopayments.sdk.core.usecase.DownloadStatementLocalUseCase
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent
import kotlinx.coroutines.launch
import java.io.File

internal class StatementDetailViewModel(
    private val statementMonth: StatementMonth,
    analyticsManager: AnalyticsServiceContract,
    private val downloadLocalUseCase: DownloadStatementLocalUseCase,
    private val downloadExternalUseCase: DownloadStatementExternalUseCase,
) : BaseViewModel() {

    private val _state = MutableLiveData(State())
    val state: LiveData<State> = _state

    val action = LiveEvent<Action>()

    init {
        analyticsManager.track(Event.MonthlyStatementsReportStart)
        downloadLocalFile()
    }

    private fun downloadLocalFile() {
        viewModelScope.launch {
            showLoading()
            downloadLocalUseCase.run(statementMonth).either(::handleFailure) {
                _state.postValue(_state.value!!.copy(file = it))
            }
            hideLoading()
        }
    }

    fun downloadToPhone() {
        viewModelScope.launch {
            showLoading()
            downloadExternalUseCase.invoke(statementMonth)
                .either(
                    { handleFailure(it) },
                    { action.postValue(Action.ShowDownloadingSign) }
                )
            hideLoading()
        }
    }

    data class State(val file: File? = null)

    sealed class Action {
        object ShowDownloadingSign : Action()
    }
}
