package com.aptopayments.sdk.features.card.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.core.usecase.CanAskBiometricsUseCase
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.repository.AuthenticationRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

internal class AccountSettingsViewModel constructor(
    private val analyticsManager: AnalyticsServiceContract,
    private val authRepository: AuthenticationRepository
) : BaseViewModel(), KoinComponent {

    val canAskBiometricsUseCase: CanAskBiometricsUseCase by inject()

    val monthlyStatementVisibility = isMonthlyStatementFlagActive()
    val securityVisibility = isSecurityAvailable()
    val fingerprintVisibility = isFingerprintAvailable()
    val notificationVisibility = isNotificationsAvailable()

    private val _fingerprintEnabled = MutableLiveData<Boolean>(true)
    val fingerprintEnabled = _fingerprintEnabled as LiveData<Boolean>

    init {
        configureFingerprint()
    }

    private fun isNotificationsAvailable() = AptoUiSdk.cardOptions.showNotificationPreferences()

    private fun configureFingerprint() {
        _fingerprintEnabled.value = canAskBiometricsUseCase().either({ false }, { it }) as Boolean
    }

    @Synchronized
    fun onFingerprintSwichTapped() {
        val currentValue = authRepository.isBiometricsEnabledByUser()
        authRepository.enableBiometrics(!currentValue)
        _fingerprintEnabled.value = !currentValue
    }

    private fun isSecurityAvailable() =
        AptoUiSdk.cardOptions.authenticateOnStartup() || AptoUiSdk.cardOptions.authenticateWithPINOnPCI()

    private fun isFingerprintAvailable() =
        canAskBiometricsUseCase().either({ false }, { it && isSecurityAvailable() }) as Boolean

    private fun isMonthlyStatementFlagActive() = AptoUiSdk.cardOptions.showMonthlyStatementOption()

    fun viewLoaded() {
        analyticsManager.track(Event.AccountSettings)
    }

}
