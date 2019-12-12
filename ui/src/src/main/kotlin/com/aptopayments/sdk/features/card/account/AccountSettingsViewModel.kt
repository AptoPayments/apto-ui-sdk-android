package com.aptopayments.sdk.features.card.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.repository.AuthenticationRepository

internal class AccountSettingsViewModel constructor(
    private val analyticsManager: AnalyticsServiceContract,
    private val authRepository: AuthenticationRepository
) : BaseViewModel() {

    private val _monthlyStatementsVisibility = MutableLiveData<Boolean>(true)
    val monthlyStatementVisibility: LiveData<Boolean>
        get() = _monthlyStatementsVisibility

    private val _securityVisibility = MutableLiveData<Boolean>(true)
    val securityVisibility: LiveData<Boolean>
        get() = _securityVisibility

    private val _fingerprintEnabled = MutableLiveData<Boolean>(true)
    val fingerprintEnabled: LiveData<Boolean>
        get() = _fingerprintEnabled

    private val _notificationsEnabled = MutableLiveData<Boolean>(true)
    val notificationsEnabled: LiveData<Boolean>
        get() = _notificationsEnabled

    init {
        configureMonthlyStatement()
        configureSecurity()
        configureFingerprint()
        configureNotifications()
    }

    private fun configureNotifications() {
        _notificationsEnabled.value = AptoUiSdk.cardOptions.showNotificationPreferences()
    }

    private fun configureFingerprint() {
        _fingerprintEnabled.value = authRepository.isBiometricsEnabledByUser()
    }

    @Synchronized
    fun onFingerprintSwichTapped() {
        val currentValue = authRepository.isBiometricsEnabledByUser()
        authRepository.enableBiometrics(!currentValue)
        _fingerprintEnabled.value = !currentValue
    }

    private fun configureSecurity() {
        _securityVisibility.value = AptoUiSdk.cardOptions.authenticateOnStartup() || AptoUiSdk.cardOptions.authenticateWithPINOnPCI()
    }

    private fun configureMonthlyStatement() {
        _monthlyStatementsVisibility.value = AptoUiSdk.cardOptions.showMonthlyStatementOption()
    }

    fun viewLoaded() {
        analyticsManager.track(Event.AccountSettings)
    }

}
