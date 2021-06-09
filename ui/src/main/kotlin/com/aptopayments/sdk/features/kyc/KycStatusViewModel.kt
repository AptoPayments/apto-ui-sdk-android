package com.aptopayments.sdk.features.kyc

import androidx.lifecycle.MutableLiveData
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.KycStatus
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class KycStatusViewModel(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    var kycStatus: MutableLiveData<KycStatus> = MutableLiveData()

    init {
        analyticsManager.track(Event.ManageCardKycStatus)
    }

    fun getKycStatus(cardId: String) {
        showLoading()
        AptoPlatform.fetchCard(cardId = cardId, forceRefresh = true) { result ->
            result.either(::handleFailure) {
                hideLoading()
                handleCard(it)
            }
        }
    }

    private fun handleCard(card: Card) {
        kycStatus.postValue(card.kycStatus)
    }
}
