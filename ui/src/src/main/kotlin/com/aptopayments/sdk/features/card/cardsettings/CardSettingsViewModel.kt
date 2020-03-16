package com.aptopayments.sdk.features.card.cardsettings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.PhoneNumber
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.card.CardDetails
import com.aptopayments.core.data.card.FeatureStatus
import com.aptopayments.core.data.cardproduct.CardProduct
import com.aptopayments.core.data.content.Content
import com.aptopayments.core.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.core.usecase.*
import com.aptopayments.sdk.core.usecase.FetchRemoteCardDetailsUseCase.Params
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.repository.IAPHelper
import com.aptopayments.sdk.utils.LiveEvent
import com.aptopayments.sdk.utils.PhoneDialer
import kotlinx.coroutines.launch
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

    private val clearCardDetails: ClearCardDetailsUseCase by inject()
    private val fetchRemoteCardDetailsUseCase: FetchRemoteCardDetailsUseCase by inject()
    private val fetchLocalCardDetailsUseCase: FetchLocalCardDetailsUseCase by inject()
    private val canAskBiometricsUseCase: CanAskBiometricsUseCase by inject()
    private val shouldAuthenticateWithPINOnPCIUseCase: ShouldAuthenticateWithPINOnPCIUseCase by inject()
    private val iapHelper: IAPHelper by inject { parametersOf(card.cardProductID) }

    val showGetPin: MutableLiveData<Boolean> = MutableLiveData()
    val showSetPin: MutableLiveData<Boolean> = MutableLiveData()
    val cardLocked: MutableLiveData<Boolean> = MutableLiveData()
    val faq: MutableLiveData<Content> = MutableLiveData()
    val cardholderAgreement: MutableLiveData<Content> = MutableLiveData()
    val privacyPolicy: MutableLiveData<Content> = MutableLiveData()
    val termsAndConditions: MutableLiveData<Content> = MutableLiveData()
    val showIvrSupport: MutableLiveData<Boolean> = MutableLiveData()
    val showAddToGooglePay = shouldShowAddToGooglePay()
    val cardDetailsFetchedCorrectly = LiveEvent<Boolean>()
    val authenticateCardDetails = LiveEvent<Boolean>()

    val hasCardDetails: LiveData<Boolean> = Transformations.map(getCardDetailsLiveData()) { details -> details != null }

    private var phoneDialer: PhoneDialer? = null

    private fun getCardDetailsLiveData() =
        fetchLocalCardDetailsUseCase().either({ MutableLiveData<CardDetails?>(null) }, { it }) as LiveData<CardDetails?>

    fun viewResumed() {
        updateViewModel()
    }

    private fun updateViewModel() {
        showGetPin.postValue(card?.features?.getPin?.status == FeatureStatus.ENABLED)
        showSetPin.postValue(card?.features?.setPin?.status == FeatureStatus.ENABLED)
        faq.postValue(cardProduct?.faq)
        cardholderAgreement.postValue(cardProduct?.cardholderAgreement)
        privacyPolicy.postValue(cardProduct?.privacyPolicy)
        termsAndConditions.postValue(cardProduct?.termsAndConditions)
        cardLocked.postValue(card?.state != Card.CardState.ACTIVE)
        showIvrSupport.postValue(card?.features?.ivrSupport?.status == FeatureStatus.ENABLED)
    }

    fun dial(phone: PhoneNumber, from: Context) {
        val phoneDialer = PhoneDialer(from)
        this.phoneDialer = phoneDialer
        phoneDialer.dialPhone(phone.toStringRepresentation(), null)
    }

    fun unlockCard(onComplete: (Unit) -> Unit) {
        card?.accountID?.let { accountId ->
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
        card?.accountID?.let { accountId ->
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

    fun cardDetailsTapped(switchValue: Boolean) {
        if (switchValue) {
            canAskBiometricsUseCase().either({}, { canAsk ->
                if (canAsk) {
                    checkIfAuthNeeded()
                } else {
                    cardDetailsAuthenticationSuccessful()
                }
            })
        } else {
            clearCardDetails()
        }
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

    fun cardDetailsAuthenticationSuccessful() {
        card?.accountID?.let { accountId ->
            viewModelScope.launch {
                showLoading()
                fetchRemoteCardDetailsUseCase(Params(accountId)).either(
                    {
                        cardDetailsFetchedCorrectly.postValue(false)
                        handleFailure(it)
                    },
                    {
                        cardDetailsFetchedCorrectly.postValue(true)
                    })
            }.invokeOnCompletion { hideLoading() }
        }
    }

    fun cardDetailsAuthenticationCancelled() {
        cardDetailsFetchedCorrectly.postValue(false)
    }

    private fun shouldShowAddToGooglePay() =
        aptoUiSdk.cardOptions.inAppProvisioningEnabled() && iapHelper.satisfyHardwareRequisites()

}
