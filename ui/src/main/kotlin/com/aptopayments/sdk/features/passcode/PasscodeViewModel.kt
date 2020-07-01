package com.aptopayments.sdk.features.passcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent

internal abstract class PasscodeViewModel(protected val analyticsManager: AnalyticsServiceContract) :
    BaseViewModel() {

    private val _title = MutableLiveData("")
    val title = _title as LiveData<String>

    private val _subtitle = MutableLiveData("")
    val subtitle = _subtitle as LiveData<String>

    protected val _showForgot = MutableLiveData(false)
    val showForgot = _showForgot as LiveData<Boolean>

    val wrongPin = LiveEvent<Boolean>()
    val backpressed = LiveEvent<Boolean>()
    val correctPin = LiveEvent<String>()

    protected lateinit var currentState: State

    val clearView = LiveEvent<Boolean>()

    protected fun setValues(title: String, subtitle: String) {
        _title.value = title
        _subtitle.value = subtitle
    }

    abstract fun viewLoaded()

    fun onPasscodeInserted(passCode: String) {
        currentState.onPasscode(passCode)
    }

    fun onBackPressed() {
        currentState.onBack()
    }

    init {
        firstState()
    }

    protected abstract fun firstState()
    protected abstract fun configureSetState()
    protected abstract fun configureConfirmState(value: String)
    abstract fun onBackFromSetState()

    protected fun clearPasscodeView() {
        clearView.postValue(true)
    }

    protected fun assignState(state: State) {
        currentState = state
        state.init()
    }

    interface State {
        fun init()
        fun getTitle(): String
        fun onPasscode(value: String)
        fun onBack()
    }

    abstract inner class SetState : State {

        override fun init() {
            clearPasscodeView()
            setValues(
                getTitle(),
                "biometric_create_pin_title_description"
            )
        }

        override fun onPasscode(value: String) {
            configureConfirmState(value)
        }

        override fun onBack() {
            onBackFromSetState()
        }
    }

    abstract inner class ConfirmState(private val firstPasscode: String) : State {
        override fun init() {
            clearPasscodeView()
            setValues(
                getTitle(),
                "biometric_create_pin_confirmation_title_description"
            )
        }

        override fun onPasscode(value: String) {
            if (firstPasscode == value) {
                correctPin.value = value
            } else {
                wrongPin.postValue(true)
                configureSetState()
            }
        }

        override fun onBack() {
            configureSetState()
        }
    }
}
