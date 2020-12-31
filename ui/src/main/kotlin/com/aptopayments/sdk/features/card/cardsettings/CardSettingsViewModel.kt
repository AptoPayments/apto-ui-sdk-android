package com.aptopayments.sdk.features.card.cardsettings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.PhoneNumber
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.FeatureStatus
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.config.ProjectConfiguration
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.core.usecase.CanAskBiometricsUseCase
import com.aptopayments.sdk.core.usecase.ShouldAuthenticateOnPCIUseCase
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.repository.IAPHelper
import com.aptopayments.sdk.repository.LocalCardDetailsRepository
import com.aptopayments.sdk.utils.LiveEvent
import com.aptopayments.sdk.utils.PhoneDialer
import com.aptopayments.sdk.utils.chatbot.ChatbotParameters
import com.aptopayments.sdk.utils.chatbot.SupportTextResolver
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

internal class CardSettingsViewModel(
    private var card: Card,
    private val cardProduct: CardProduct,
    private val projectConfiguration: ProjectConfiguration,
    private val analyticsManager: AnalyticsServiceContract,
    private val aptoPlatform: AptoPlatformProtocol,
    private val aptoUiSdk: AptoUiSdkProtocol
) : BaseViewModel(), KoinComponent {

    private val canAskBiometricsUseCase: CanAskBiometricsUseCase by inject()
    private val shouldAuthenticateWithOnPCIUseCase: ShouldAuthenticateOnPCIUseCase by inject()
    private val iapHelper: IAPHelper by inject { parametersOf(card.cardProductID) }
    private val supportTextResolver: SupportTextResolver by inject { parametersOf(projectConfiguration.isChatbotActive) }

    private val cardDetailsRepo: LocalCardDetailsRepository by inject()

    private val _cardUiState = MutableLiveData<CardUiState>()
    val cardUiState = _cardUiState as LiveData<CardUiState>

    val showLegalSection = shouldShowLegalSection(cardProduct)
    val showFaq = cardProduct.faq != null
    val showCardholderAgreement = cardProduct.cardholderAgreement != null
    val showTermsAndConditions = cardProduct.termsAndConditions != null
    val showPrivacyPolicy = cardProduct.privacyPolicy != null
    val supportTexts = supportTextResolver.getTexts()

    val showAddToGooglePay = shouldShowAddToGooglePay()

    val action = LiveEvent<Action>()

    init {
        updateCardValues(card)
    }

    fun dial(phone: PhoneNumber, from: Context) {
        val phoneDialer = PhoneDialer(from)
        phoneDialer.dialPhone(phone.toStringRepresentation(), null)
    }

    fun unlockCard() {
        showLoading()
        aptoPlatform.unlockCard(card.accountID) { result ->
            hideLoading()
            onLockUnlockFinished(result)
        }
    }

    fun lockCard() {
        showLoading()
        aptoPlatform.lockCard(card.accountID) { result ->
            hideLoading()
            onLockUnlockFinished(result)
        }
    }

    fun viewLoaded() {
        analyticsManager.track(Event.ManageCardCardSettings)
    }

    fun cardDetailsPressed() {
        canAskBiometricsUseCase().either(
            {},
            { canAsk ->
                if (canAsk) {
                    checkIfAuthNeeded()
                } else {
                    cardDetailsAuthenticationSuccessful()
                }
            }
        )
    }

    fun onFaqPressed() {
        showContentPresenter(cardProduct.faq, "card_settings_legal_faq_title")
    }

    fun onCardholderAgreementPressed() {
        showContentPresenter(cardProduct.cardholderAgreement, "card_settings_legal_cardholder_agreement_title")
    }

    fun onPrivacyPolicyPressed() {
        showContentPresenter(cardProduct.privacyPolicy, "card_settings_legal_privacy_policy_title")
    }

    fun onTermsPressed() {
        showContentPresenter(cardProduct.termsAndConditions, "card_settings_legal_terms_of_service_title")
    }

    private fun showContentPresenter(content: Content?, title: String) {
        content?.let {
            action.value = Action.ContentPresenter(it, title)
        }
    }

    private fun updateCardValues(card: Card) {
        _cardUiState.value =
            CardUiState(
                showGetPin = card.features?.getPin?.status == FeatureStatus.ENABLED,
                showSetPin = card.features?.setPin?.status == FeatureStatus.ENABLED,
                showIvrSupport = card.features?.ivrSupport?.status == FeatureStatus.ENABLED,
                cardLocked = card.state != Card.CardState.ACTIVE,
                showAddFunds = card.features?.funding?.isEnabled ?: false,
                showPasscode = card.features?.passcode?.isEnabled ?: false
            )
    }

    private fun shouldShowLegalSection(cardProduct: CardProduct) =
        cardProduct.cardholderAgreement != null || cardProduct.termsAndConditions != null || cardProduct.privacyPolicy != null

    private fun updateCard(card: Card) {
        this.card = card
        updateCardValues(card)
    }

    private fun checkIfAuthNeeded() {
        shouldAuthenticateWithOnPCIUseCase().runIfRight { needsAuthenticate ->
            if (needsAuthenticate) {
                action.postValue(Action.AuthenticateCardDetails)
            } else {
                cardDetailsAuthenticationSuccessful()
            }
        }
    }

    private fun shouldShowAddToGooglePay() =
        aptoUiSdk.cardOptions.inAppProvisioningEnabled() && iapHelper.satisfyHardwareRequisites()

    fun cardDetailsAuthenticationSuccessful() {
        cardDetailsRepo.showCardDetails()
        action.postValue(Action.ShowCardDetails)
    }

    fun cardDetailsAuthenticationError() {
        cardDetailsRepo.hideCardDetails()
    }

    fun setPasscodePressed() {
        if (card.state == Card.CardState.ACTIVE) {
            action.value = Action.SetCardPasscode
        } else {
            action.value = Action.SetCardPasscodeErrorDisabled
        }
    }

    private fun onLockUnlockFinished(result: Either<Failure, Card>) {
        result.either(::handleFailure) { card ->
            updateCard(card)
            action.postValue(Action.CardStateChanged)
        }
    }

    fun onCustomerSupport() {
        action.postValue(getCustomerSupportAction())
    }

    private fun getCustomerSupportAction(): Action {
        return if (projectConfiguration.isChatbotActive) {
            Action.StartChatbot(
                ChatbotParameters(
                    name = card.cardHolder,
                    cardId = card.accountID,
                    cardProductId = cardProduct.id
                )
            )
        } else {
            Action.CustomerSupportEmail
        }
    }

    internal sealed class Action {
        class ContentPresenter(val content: Content, val title: String) : Action()
        object ShowCardDetails : Action()
        object AuthenticateCardDetails : Action()
        object SetCardPasscode : Action()
        object CardStateChanged : Action()
        object SetCardPasscodeErrorDisabled : Action()
        class StartChatbot(val param: ChatbotParameters) : Action()
        object CustomerSupportEmail : Action()
    }

    internal data class CardUiState(
        val showGetPin: Boolean = false,
        val showSetPin: Boolean = false,
        val showIvrSupport: Boolean = false,
        val cardLocked: Boolean = false,
        val showAddFunds: Boolean = false,
        val showPasscode: Boolean = false
    )
}
