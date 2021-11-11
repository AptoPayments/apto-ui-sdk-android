package com.aptopayments.sdk.features.card.cardsettings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.PhoneNumber
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.Disclaimer
import com.aptopayments.mobile.data.card.FeatureStatus
import com.aptopayments.mobile.data.card.FeatureType
import com.aptopayments.mobile.data.cardproduct.CardProduct
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
import com.aptopayments.sdk.utils.extensions.shouldShowFakeShippingStatus
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalDate

internal class CardSettingsViewModel(
    var card: Card,
    private val cardProduct: CardProduct,
    private val analyticsManager: AnalyticsServiceContract,
    private val aptoPlatform: AptoPlatformProtocol,
    private val aptoUiSdk: AptoUiSdkProtocol
) : BaseViewModel(), KoinComponent {

    private val canAskBiometricsUseCase: CanAskBiometricsUseCase by inject()
    private val shouldAuthenticateWithOnPCIUseCase: ShouldAuthenticateOnPCIUseCase by inject()
    private val iapHelper: IAPHelper by inject { parametersOf(card.cardProductID) }
    private val telephonyEnabledChecker: TelephonyEnabledChecker by inject()

    private val cardDetailsRepo: LocalCardDetailsRepository by inject()

    private val _state = MutableLiveData<State>()
    val state = _state as LiveData<State>

    val action = LiveEvent<Action>()

    init {
        updateCardValues(card)
        analyticsManager.track(Event.ManageCardCardSettings)
    }

    fun getPinPressed() {
        when (val type = card.features?.getPin?.type) {
            is FeatureType.Voip -> action.postValue(Action.CallVoIpListenPin)
            is FeatureType.Ivr -> onGetPinWithIvr(type)
        }
    }

    private fun onGetPinWithIvr(type: FeatureType.Ivr) {
        if (telephonyEnabledChecker.isEnabled()) {
            type.ivrPhone?.let {
                action.postValue(Action.CallIvr(it))
            }
        } else {
            action.postValue(Action.ShowNoSimInsertedError)
        }
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

    fun cardDetailsPressed() {
        canAskBiometricsUseCase().runIfRight { canAsk ->
            if (canAsk) {
                checkIfAuthNeeded()
            } else {
                cardDetailsAuthenticationSuccessful()
            }
        }
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

    fun onExchangeRatesPressed() {
        showContentPresenter(cardProduct.exchangeRates, "card_settings_legal_exchange_rates_title")
    }

    private fun showContentPresenter(content: Content?, title: String) {
        content?.let {
            action.value = Action.ContentPresenter(it, title)
        }
    }

    private fun updateCardValues(card: Card) {
        _state.value =
            State(
                showGetPin = card.features?.getPin?.status == FeatureStatus.ENABLED,
                showSetPin = card.features?.setPin?.status == FeatureStatus.ENABLED,
                showIvrSupport = isIvrEnabled(card),
                cardLocked = card.state != Card.CardState.ACTIVE,
                showAddFunds = card.features?.funding?.isEnabled ?: false,
                showPasscode = card.features?.passcode?.isEnabled ?: false,
                showMonthlyStatement = aptoUiSdk.cardOptions.showMonthlyStatementOption(),
                showOrderPhysical = card.orderedStatus == Card.OrderedStatus.AVAILABLE,
                showLegalSection = shouldShowLegalSection(cardProduct),
                showFaq = cardProduct.faq != null,
                showCardholderAgreement = cardProduct.cardholderAgreement != null,
                showTermsAndConditions = cardProduct.termsAndConditions != null,
                showPrivacyPolicy = cardProduct.privacyPolicy != null,
                showExchangeRates = cardProduct.exchangeRates != null,
                showAddToGooglePay = shouldShowAddToGooglePay(),
                showCardShippingStatus = card.shouldShowFakeShippingStatus(LocalDate.now()),
                showTransferMoney = card.features?.transferMoneyP2p?.isEnabled ?: false,
            )
    }

    private fun isIvrEnabled(card: Card): Boolean {
        return card.features?.ivrSupport?.status == FeatureStatus.ENABLED && card.features?.ivrSupport?.ivrPhone != null
    }

    private fun shouldShowLegalSection(cardProduct: CardProduct) =
        cardProduct.cardholderAgreement != null ||
            cardProduct.termsAndConditions != null ||
            cardProduct.privacyPolicy != null ||
            cardProduct.exchangeRates != null

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

    private fun shouldShowAddToGooglePay() = iapHelper.satisfyHardwareRequisites() &&
        card.features?.inAppProvisioning?.isEnabled == true

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

    fun onAddFundsPressed() {
        val nextAction = if (card.features?.achAccount?.isEnabled == true) {
            if (card.features?.achAccount?.isAccountProvisioned == true) {
                Action.ShowAddFundsSelector
            } else {
                Action.ShowAddFundsAchDisclaimer(card.features?.achAccount?.disclaimer)
            }
        } else {
            Action.AddFunds
        }
        action.postValue(nextAction)
    }

    fun onTransferMoneyPressed() {
        action.postValue(Action.TransferMoneyAction)
    }

    private fun onLockUnlockFinished(result: Either<Failure, Card>) {
        result.either(::handleFailure) { card ->
            updateCard(card)
            action.postValue(Action.CardStateChanged)
        }
    }

    fun onCustomerSupport() {
        action.postValue(Action.CustomerSupportEmail)
    }

    fun onIvrSupportClicked() {
        val phone = card.features?.ivrSupport?.ivrPhone
        val nextAction = if (telephonyEnabledChecker.isEnabled() && phone != null) {
            Action.CallIvr(phone)
        } else {
            Action.ShowNoSimInsertedError
        }
        action.postValue(nextAction)
    }

    fun orderPhysicalCard() {
        action.postValue(Action.OrderPhysicalCard)
    }

    fun onPresented() {
        aptoPlatform.fetchCard(card.accountID, forceRefresh = false) { result -> result.runIfRight { updateCard(it) } }
    }

    internal sealed class Action {
        class ContentPresenter(val content: Content, val title: String) : Action()
        object ShowCardDetails : Action()
        object AuthenticateCardDetails : Action()
        object SetCardPasscode : Action()
        object CardStateChanged : Action()
        object SetCardPasscodeErrorDisabled : Action()
        object CustomerSupportEmail : Action()
        class CallIvr(val phoneNumber: PhoneNumber) : Action()
        object ShowNoSimInsertedError : Action()
        object CallVoIpListenPin : Action()
        object AddFunds : Action()
        object ShowAddFundsSelector : Action()
        class ShowAddFundsAchDisclaimer(val disclaimer: Disclaimer?) : Action()
        object OrderPhysicalCard : Action()
        object TransferMoneyAction : Action()
    }

    internal data class State(
        val showGetPin: Boolean = false,
        val showSetPin: Boolean = false,
        val showIvrSupport: Boolean = false,
        val cardLocked: Boolean = false,
        val showAddFunds: Boolean = false,
        val showPasscode: Boolean = false,
        val showMonthlyStatement: Boolean = false,
        val showOrderPhysical: Boolean = false,
        val showLegalSection: Boolean = false,
        val showFaq: Boolean = false,
        val showCardholderAgreement: Boolean = false,
        val showTermsAndConditions: Boolean = false,
        val showPrivacyPolicy: Boolean = false,
        val showExchangeRates: Boolean = false,
        val showAddToGooglePay: Boolean = false,
        val showCardShippingStatus: Boolean = false,
        val showTransferMoney: Boolean = false
    )
}
