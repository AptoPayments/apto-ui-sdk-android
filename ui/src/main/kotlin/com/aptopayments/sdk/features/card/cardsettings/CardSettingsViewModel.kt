package com.aptopayments.sdk.features.card.cardsettings

import android.content.Context
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
    private var cardProduct: CardProduct,
    private val analyticsManager: AnalyticsServiceContract,
    private val aptoPlatform: AptoPlatformProtocol,
    private val aptoUiSdk: AptoUiSdkProtocol
) : BaseViewModel(), KoinComponent {

    private val canAskBiometricsUseCase: CanAskBiometricsUseCase by inject()
    private val shouldAuthenticateWithPINOnPCIUseCase: ShouldAuthenticateWithPINOnPCIUseCase by inject()
    private val iapHelper: IAPHelper by inject { parametersOf(card.cardProductID) }

    private val cardDetailsRepo: LocalCardDetailsRepository by inject()

    val showGetPin: MutableLiveData<Boolean> = MutableLiveData()
    val showSetPin: MutableLiveData<Boolean> = MutableLiveData()
    val cardLocked: MutableLiveData<Boolean> = MutableLiveData()
    val faq: MutableLiveData<Content> = MutableLiveData()
    val cardholderAgreement: MutableLiveData<Content> = MutableLiveData()
    val privacyPolicy: MutableLiveData<Content> = MutableLiveData()
    val termsAndConditions: MutableLiveData<Content> = MutableLiveData()
    val showIvrSupport: MutableLiveData<Boolean> = MutableLiveData()
    val showAddToGooglePay = shouldShowAddToGooglePay()
    val cardDetailsClicked = LiveEvent<Boolean>()
    val authenticateCardDetails = LiveEvent<Boolean>()

    private var phoneDialer: PhoneDialer? = null

    fun viewResumed() {
        updateViewModel()
    }

    private fun updateViewModel() {
        showGetPin.postValue(card.features?.getPin?.status == FeatureStatus.ENABLED)
        showSetPin.postValue(card.features?.setPin?.status == FeatureStatus.ENABLED)
        faq.postValue(cardProduct.faq)
        cardholderAgreement.postValue(cardProduct.cardholderAgreement)
        privacyPolicy.postValue(cardProduct.privacyPolicy)
        termsAndConditions.postValue(cardProduct.termsAndConditions)
        cardLocked.postValue(card.state != Card.CardState.ACTIVE)
        showIvrSupport.postValue(card.features?.ivrSupport?.status == FeatureStatus.ENABLED)
    }

    fun dial(phone: PhoneNumber, from: Context) {
        val phoneDialer = PhoneDialer(from)
        this.phoneDialer = phoneDialer
        phoneDialer.dialPhone(phone.toStringRepresentation(), null)
    }

    fun unlockCard(onComplete: (Unit) -> Unit) {
        card.accountID.let { accountId ->
            aptoPlatform.unlockCard(accountId) { result ->
                result.either(::handleFailure) {
                    this.card = it
                    updateViewModel()
                    onComplete(Unit)
                }
            }
        }
    }

    fun lockCard(onComplete: (Unit) -> Unit) {
        card.accountID.let { accountId ->
            aptoPlatform.lockCard(accountId) { result ->
                result.either(::handleFailure) {
                    this.card = it
                    updateViewModel()
                    onComplete(Unit)
                }
            }
        }
    }

    fun viewLoaded() {
        analyticsManager.track(Event.ManageCardCardSettings)
    }

    fun cardDetailsTapped() {
        canAskBiometricsUseCase().either({}, { canAsk ->
            if (canAsk) {
                checkIfAuthNeeded()
            } else {
                cardDetailsAuthenticationSuccessful()
            }
        })
    }

    private fun checkIfAuthNeeded() {
        shouldAuthenticateWithPINOnPCIUseCase().either(
            {},
            { needsAuthenticate ->
                if (needsAuthenticate) {
                    authenticateCardDetails.postValue(true)
                } else {
                    cardDetailsAuthenticationSuccessful()
                }
            }
        )
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
