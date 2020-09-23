package com.aptopayments.sdk.features.card.cardsettings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.PhoneNumber
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.FeatureStatus
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.core.usecase.CanAskBiometricsUseCase
import com.aptopayments.sdk.core.usecase.ShouldAuthenticateWithPINOnPCIUseCase
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.repository.IAPHelper
import com.aptopayments.sdk.repository.LocalCardDetailsRepository
import com.aptopayments.sdk.utils.LiveEvent
import com.aptopayments.sdk.utils.PhoneDialer
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

internal class CardSettingsViewModel constructor(
    private var card: Card,
    private val cardProduct: CardProduct,
    private val analyticsManager: AnalyticsServiceContract,
    private val aptoPlatform: AptoPlatformProtocol,
    private val aptoUiSdk: AptoUiSdkProtocol
) : BaseViewModel(), KoinComponent {

    private val canAskBiometricsUseCase: CanAskBiometricsUseCase by inject()
    private val shouldAuthenticateWithPINOnPCIUseCase: ShouldAuthenticateWithPINOnPCIUseCase by inject()
    private val iapHelper: IAPHelper by inject { parametersOf(card.cardProductID) }

    private val cardDetailsRepo: LocalCardDetailsRepository by inject()

    private val _showGetPin = MutableLiveData(false)
    private val _showSetPin = MutableLiveData(false)
    private val _showIvrSupport = MutableLiveData(false)
    private val _showAddFunds = MutableLiveData(false)
    private val _cardLocked = MutableLiveData(false)

    val showGetPin: LiveData<Boolean> = _showGetPin
    val showSetPin: LiveData<Boolean> = _showSetPin
    val showIvrSupport: LiveData<Boolean> = _showIvrSupport
    val showAddFunds: LiveData<Boolean> = _showAddFunds
    val cardLocked: LiveData<Boolean> = _cardLocked

    val showLegalSection = shouldShowLegalSection(cardProduct)
    val showFaq = cardProduct.faq != null
    val showCardholderAgreement = cardProduct.cardholderAgreement != null
    val showTermsAndConditions = cardProduct.termsAndConditions != null
    val showPrivacyPolicy = cardProduct.privacyPolicy != null

    val showAddToGooglePay = shouldShowAddToGooglePay()

    val cardDetailsClicked = LiveEvent<Boolean>()
    val authenticateCardDetails = LiveEvent<Boolean>()
    val showContentPresenter = LiveEvent<Pair<Content, String>>()

    init {
        updateCardValues(card)
    }

    fun dial(phone: PhoneNumber, from: Context) {
        val phoneDialer = PhoneDialer(from)
        phoneDialer.dialPhone(phone.toStringRepresentation(), null)
    }

    fun unlockCard(onComplete: (Unit) -> Unit) {
        card.accountID.let { accountId ->
            aptoPlatform.unlockCard(accountId) { result ->
                result.either(::handleFailure) {
                    updateCard(it)
                    onComplete(Unit)
                }
            }
        }
    }

    fun lockCard(onComplete: (Unit) -> Unit) {
        card.accountID.let { accountId ->
            aptoPlatform.lockCard(accountId) { result ->
                result.either(::handleFailure) {
                    updateCard(card)
                    onComplete(Unit)
                }
            }
        }
    }

    fun viewLoaded() {
        analyticsManager.track(Event.ManageCardCardSettings)
    }

    fun cardDetailsPressed() {
        canAskBiometricsUseCase().either({}, { canAsk ->
            if (canAsk) {
                checkIfAuthNeeded()
            } else {
                cardDetailsAuthenticationSuccessful()
            }
        })
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
        content?.let { showContentPresenter.value = Pair(it, title) }
    }

    private fun updateCardValues(card: Card) {
        _showGetPin.postValue(card.features?.getPin?.status == FeatureStatus.ENABLED)
        _showSetPin.postValue(card.features?.setPin?.status == FeatureStatus.ENABLED)
        _showIvrSupport.postValue(card.features?.ivrSupport?.status == FeatureStatus.ENABLED)
        _cardLocked.postValue(card.state != Card.CardState.ACTIVE)
        _showAddFunds.postValue(card.features?.funding?.isEnabled)
    }

    private fun shouldShowLegalSection(cardProduct: CardProduct) =
        cardProduct.cardholderAgreement != null || cardProduct.termsAndConditions != null || cardProduct.privacyPolicy != null

    private fun updateCard(card: Card) {
        this.card = card
        updateCardValues(card)
    }

    private fun checkIfAuthNeeded() {
        shouldAuthenticateWithPINOnPCIUseCase().runIfRight { needsAuthenticate ->
            if (needsAuthenticate) {
                authenticateCardDetails.postValue(true)
            } else {
                cardDetailsAuthenticationSuccessful()
            }
        }
    }

    private fun shouldShowAddToGooglePay() =
        aptoUiSdk.cardOptions.inAppProvisioningEnabled() && iapHelper.satisfyHardwareRequisites()

    fun cardDetailsAuthenticationSuccessful() {
        cardDetailsRepo.showCardDetails()
        cardDetailsClicked.postValue(true)
    }

    fun cardDetailsAuthenticationError() {
        cardDetailsRepo.hideCardDetails()
    }
}
