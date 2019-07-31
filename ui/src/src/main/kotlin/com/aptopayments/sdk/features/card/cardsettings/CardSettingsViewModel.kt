package com.aptopayments.sdk.features.card.cardsettings

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.data.PhoneNumber
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.card.CardDetails
import com.aptopayments.core.data.card.FeatureStatus
import com.aptopayments.core.data.cardproduct.CardProduct
import com.aptopayments.core.data.content.Content
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.utils.PhoneDialer
import javax.inject.Inject

internal class CardSettingsViewModel @Inject constructor(
        private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    var showGetPin: MutableLiveData<Boolean> = MutableLiveData()
    var showSetPin: MutableLiveData<Boolean> = MutableLiveData()
    var cardLocked: MutableLiveData<Boolean> = MutableLiveData()
    var faq: MutableLiveData<Content> = MutableLiveData()
    var cardholderAgreement: MutableLiveData<Content> = MutableLiveData()
    var privacyPolicy: MutableLiveData<Content> = MutableLiveData()
    var termsAndConditions: MutableLiveData<Content> = MutableLiveData()
    var cardDetails: MutableLiveData<CardDetails?> = MutableLiveData()
    var cardDetailsShown: MutableLiveData<Boolean> = MutableLiveData()
    var showIvrSupport: MutableLiveData<Boolean> = MutableLiveData()

    private var card: Card? = null
    private var cardProduct: CardProduct? = null
    private var phoneDialer: PhoneDialer? = null

    fun viewResumed(card: Card, cardDetailsShown: Boolean, cardProduct: CardProduct) {
        this.card = card
        this.cardProduct = cardProduct
        this.cardDetailsShown.postValue(cardDetailsShown)
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
        card?.accountID?.let{ accountId ->
            AptoPlatform.unlockCard(accountId) { result ->
                result.either(::handleFailure) {
                    this.card = it
                    updateViewModel()
                    onComplete(Unit)
                }
            }
        }
    }

    fun lockCard(onComplete: (Unit) -> Unit) {
        card?.accountID?.let{ accountId ->
            AptoPlatform.lockCard(accountId) { result ->
                result.either(::handleFailure) {
                    this.card = it
                    updateViewModel()
                    onComplete(Unit)
                }
            }
        }
    }

    fun hideCardDetails() {
        cardDetailsShown.postValue(false)
        cardDetails.postValue(null)
    }

    fun getCardDetails(onComplete: () -> Unit) {
        card?.accountID?.let { accountId ->
            AptoPlatform.fetchCardDetails(accountId) { result ->
                onComplete()
                result.either(::handleFailure) {
                    cardDetailsShown.postValue(true)
                    cardDetails.postValue(it)
                }
            }
        }
    }

    fun viewLoaded() {
        analyticsManager.track(Event.ManageCardCardSettings)
    }
}
