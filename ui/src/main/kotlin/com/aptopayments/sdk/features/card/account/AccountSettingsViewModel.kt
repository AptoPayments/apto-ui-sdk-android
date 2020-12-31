package com.aptopayments.sdk.features.card.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.config.ProjectConfiguration
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.getOrElse
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.core.usecase.ShouldShowBiometricOption
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.repository.AuthenticationRepository
import com.aptopayments.sdk.utils.LiveEvent
import com.aptopayments.sdk.utils.chatbot.ChatbotParameters
import com.aptopayments.sdk.utils.chatbot.SupportTextResolver
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf
import java.io.Serializable
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class AccountSettingsViewModel(
    private val cardId: String,
    private val projectConfiguration: ProjectConfiguration,
    private val aptoPlatform: AptoPlatformProtocol,
    private val analyticsManager: AnalyticsServiceContract,
    private val authRepository: AuthenticationRepository,
    private val showBiometricOption: ShouldShowBiometricOption
) : BaseViewModel(), KoinComponent {

    private val supportTextResolver: SupportTextResolver by inject { parametersOf(projectConfiguration.isChatbotActive) }

    val monthlyStatementVisibility = isMonthlyStatementFlagActive()
    val securityVisibility = isSecurityAvailable()
    val fingerprintVisibility = isFingerprintAvailable()
    val notificationVisibility = isNotificationsAvailable()
    val supportTexts = supportTextResolver.getTexts()

    private val _fingerprintEnabled = MutableLiveData(true)
    val fingerprintEnabled = _fingerprintEnabled as LiveData<Boolean>
    val action = LiveEvent<Action>()

    init {
        configureFingerprint()
    }

    private fun isNotificationsAvailable() = AptoUiSdk.cardOptions.showNotificationPreferences()

    private fun configureFingerprint() {
        _fingerprintEnabled.value = authRepository.isBiometricsEnabledByUser()
    }

    @Synchronized
    fun onFingerprintSwitchTapped() {
        val currentValue = authRepository.isBiometricsEnabledByUser()
        authRepository.enableBiometrics(!currentValue)
        _fingerprintEnabled.value = !currentValue
    }

    private fun isSecurityAvailable() =
        AptoUiSdk.cardOptions.authenticateOnStartup() || AptoUiSdk.cardOptions.authenticatePCI() == CardOptions.PCIAuthType.PIN_OR_BIOMETRICS

    private fun isFingerprintAvailable() = showBiometricOption().either({ false }, { it })

    private fun isMonthlyStatementFlagActive() = AptoUiSdk.cardOptions.showMonthlyStatementOption()

    fun viewLoaded() {
        analyticsManager.track(Event.AccountSettings)
    }

    fun onCustomerSupport() {
        if (projectConfiguration.isChatbotActive) {
            setupAndLaunchChatbot()
        } else {
            action.postValue(Action.CustomerSupportEmail)
        }
    }

    private fun setupAndLaunchChatbot() {
        viewModelScope.launch {
            showLoading()
            var cardProduct: CardProduct? = null
            val card = fetchCard(cardId)

            if (card == null) {
                handleFailure(CardNotFound())
            } else {
                card.cardProductID?.let {
                    cardProduct = fetchCardProduct(it)
                }
                launchChatbot(card, cardProduct)
            }
            hideLoading()
        }
    }

    private fun launchChatbot(
        card: Card,
        cardProduct: CardProduct?
    ) {
        action.postValue(Action.LaunchChatbot(ChatbotParameters(card.cardHolder, card.accountID, cardProduct?.id)))
    }

    private suspend fun fetchCard(cardId: String): Card? = suspendCoroutine { cont ->
        aptoPlatform.fetchCard(cardId, false) { result ->
            cont.resume(result.getOrElse { null })
        }
    }

    private suspend fun fetchCardProduct(id: String): CardProduct? = suspendCoroutine { cont ->
        aptoPlatform.fetchCardProduct(id, false) { result ->
            cont.resume(result.getOrElse { null })
        }
    }

    internal class CardNotFound : Failure.FeatureFailure()

    internal sealed class Action : Serializable {
        class LaunchChatbot(val param: ChatbotParameters) : Action()
        object CustomerSupportEmail : Action()
    }
}
