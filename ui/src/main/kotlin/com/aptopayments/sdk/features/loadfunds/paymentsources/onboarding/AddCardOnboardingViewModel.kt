package com.aptopayments.sdk.features.loadfunds.paymentsources.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.extension.localized
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourcesRepository
import com.aptopayments.sdk.utils.LiveEvent
import com.aptopayments.sdk.utils.extensions.setValue

internal class AddCardOnboardingViewModel(
    cardId: String,
    private val repository: PaymentSourcesRepository,
    private val aptoPlatform: AptoPlatformProtocol
) : BaseViewModel() {

    private val _firstDescription = MutableLiveData("")
    private val _secondDescription = MutableLiveData("")
    val firstDescription = _firstDescription as LiveData<String>
    val secondDescription = _secondDescription as LiveData<String>
    val actions = LiveEvent<Actions>()

    init {
        fetchNamesAndPublishDescriptions(cardId)
    }

    fun onContinueClicked() {
        repository.acceptedOnboarding()
        actions.postValue(Actions.Continue)
    }

    private fun fetchNamesAndPublishDescriptions(cardId: String) {
        showLoading()
        aptoPlatform.fetchCard(cardId, false) { result ->
            hideLoading()
            result.either(
                { handleFailure(it) },
                { card ->
                    _secondDescription.value = createSecondDescription(card.features?.funding?.softDescriptor ?: "")
                    fetchCompanyName(card.cardProductID)
                }
            )
        }
    }

    private fun fetchCompanyName(cardProductId: String?) {
        cardProductId?.let {
            aptoPlatform.fetchCardProduct(cardProductId, false) { result ->
                result.either(
                    { handleFailure(it) },
                    { cardProduct ->
                        _firstDescription.value = createFirstDescription(cardProduct.name)
                    }
                )
            }
        }
    }

    private fun createFirstDescription(companyName: String) =
        "load_funds_add_card_onboarding_explanation".localized().setValue(companyName)

    private fun createSecondDescription(softDescriptor: String) =
        "load_funds_add_card_onboarding_explanation_2".localized().setValue(softDescriptor)

    sealed class Actions {
        object Continue : Actions()
    }
}
