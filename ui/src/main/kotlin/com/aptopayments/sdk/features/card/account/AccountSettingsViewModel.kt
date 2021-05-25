package com.aptopayments.sdk.features.card.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.core.usecase.ShouldShowBiometricOption
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.repository.AuthenticationRepository
import com.aptopayments.sdk.utils.LiveEvent
import org.koin.core.KoinComponent
import java.io.Serializable

internal class AccountSettingsViewModel(
    analyticsManager: AnalyticsServiceContract,
    private val authRepository: AuthenticationRepository,
    private val showBiometricOption: ShouldShowBiometricOption,
    private val aptoUiSdk: AptoUiSdkProtocol,
) : BaseViewModel(), KoinComponent {

    val uiState = createUiState()

    private val _fingerprintEnabled = MutableLiveData(true)
    val fingerprintEnabled = _fingerprintEnabled as LiveData<Boolean>
    val action = LiveEvent<Action>()

    init {
        configureFingerprint()
        analyticsManager.track(Event.AccountSettings)
    }

    private fun createUiState(): UiState {
        return UiState(
            securityVisibility = isSecurityAvailable(),
            notificationVisibility = isNotificationsAvailable(),
            fingerprintVisibility = isFingerprintAvailable(),
        )
    }

    private fun isNotificationsAvailable() = aptoUiSdk.cardOptions.showNotificationPreferences()

    private fun configureFingerprint() {
        _fingerprintEnabled.value = authRepository.isBiometricsEnabledByUser()
    }

    @Synchronized
    fun onFingerprintSwitchTapped(value: Boolean) {
        authRepository.enableBiometrics(value)
        _fingerprintEnabled.value = value
    }

    private fun isSecurityAvailable() =
        aptoUiSdk.cardOptions.authenticateOnStartup() ||
            aptoUiSdk.cardOptions.authenticatePCI() == CardOptions.PCIAuthType.PIN_OR_BIOMETRICS

    private fun isFingerprintAvailable() = showBiometricOption().either({ false }, { it })

    fun onCustomerSupport() {
        action.postValue(Action.CustomerSupportEmail)
    }

    internal sealed class Action : Serializable {
        object CustomerSupportEmail : Action()
    }

    data class UiState(
        val securityVisibility: Boolean = false,
        val notificationVisibility: Boolean = false,
        val fingerprintVisibility: Boolean = false,
    )
}
