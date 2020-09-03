package com.aptopayments.sdk.features.loadfunds.paymentsources

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.AddCardPaymentSourceContract
import com.aptopayments.sdk.features.loadfunds.paymentsources.onboarding.AddCardOnboardingContract
import org.koin.core.inject

private const val ADD_CARD_TAG = "AddCardDetailsFragment"
private const val ADD_CARD_ONBOARDING_TAG = "AddCardOnboardingFragment"

internal class AddPaymentSourceFlow(private val cardId: String, private var onClose: () -> Unit) : Flow(),
    AddCardPaymentSourceContract.Delegate, AddCardOnboardingContract.Delegate {

    private val repository: PaymentSourcesRepository by inject()

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = getFirstFragment()
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    private fun getFirstFragment(): Any {
        return if (repository.hasAcceptedOnboarding()) {
            getAddCardFragment()
        } else {
            getOnboardingFragment()
        }
    }

    private fun getAddCardFragment(): AddCardPaymentSourceContract.View {
        val fragment = fragmentFactory.addCardDetailsFragment(cardId = cardId, tag = ADD_CARD_TAG)
        fragment.delegate = this
        return fragment
    }

    private fun getOnboardingFragment(): AddCardOnboardingContract.View {
        val fragment = fragmentFactory.addCardOnboardingFragment(cardId = cardId, tag = ADD_CARD_TAG)
        fragment.delegate = this
        return fragment
    }

    override fun restoreState() {
        (fragmentWithTag(ADD_CARD_TAG) as? AddCardPaymentSourceContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(ADD_CARD_ONBOARDING_TAG) as? AddCardOnboardingContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onCardAdded() {
        onClose()
    }

    override fun onBackFromSaveCard() {
        onClose()
    }

    override fun onBackAddCardOnboarding() {
        onClose()
    }

    override fun onContinueAddCardOnboarding() {
        push(getAddCardFragment() as BaseFragment)
    }
}
