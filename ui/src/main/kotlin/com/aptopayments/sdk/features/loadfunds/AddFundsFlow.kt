package com.aptopayments.sdk.features.loadfunds

import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.data.payment.Payment
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.extension.localized
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.contentpresenter.ContentPresenterContract
import com.aptopayments.sdk.features.loadfunds.add.AddFundsContract
import com.aptopayments.sdk.features.loadfunds.paymentsources.AddPaymentSourceFlow
import com.aptopayments.sdk.features.loadfunds.paymentsources.list.PaymentSourcesListContract
import com.aptopayments.sdk.features.loadfunds.result.AddFundsResultContract

private const val PAYMENT_SOURCES_LIST_TAG = "PaymentSourcesListFragment"
private const val ADD_FUNDS_TAG = "AddFundsFragment"
private const val RESULT_TAG = "AddFundsResultFragment"
private const val CONTENT_PRESENTER_TAG = "ContentPresenterFragment"

internal class AddFundsFlow(private val cardId: String, private var onClose: () -> Unit) :
    Flow(),
    PaymentSourcesListContract.Delegate,
    AddFundsContract.Delegate,
    AddFundsResultContract.Delegate,
    ContentPresenterContract.Delegate {

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = fragmentFactory.addFundsFragment(
            cardId = cardId,
            tag = ADD_FUNDS_TAG
        )
        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(ADD_FUNDS_TAG) as? AddFundsContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(PAYMENT_SOURCES_LIST_TAG) as? PaymentSourcesListContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(RESULT_TAG) as? AddFundsResultContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(CONTENT_PRESENTER_TAG) as? ContentPresenterContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onClosePaymentSourcesList() {
        popFragment()
    }

    override fun newCardPressed() {
        openNewCard()
    }

    private fun openNewCard() {
        val flow = AddPaymentSourceFlow(
            cardId = cardId,
            onClose = {
                popFlow(true)
            }
        )
        flow.init { initResult -> initResult.either(::handleFailure) { push(flow) } }
    }

    override fun onPaymentResult(payment: Payment) {
        val fragment = fragmentFactory.addFundsResultFragment(
            cardId = cardId,
            payment = payment,
            tag = PAYMENT_SOURCES_LIST_TAG
        )
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun onPaymentSourcesList() {
        val fragment = fragmentFactory.paymentSourcesList(tag = PAYMENT_SOURCES_LIST_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun onAddPaymenSource() {
        openNewCard()
    }

    override fun onBackFromAddFunds() {
        onClose()
    }

    override fun onBackFromAddFundsResult() {
        onClose()
    }

    override fun onCardholderAgreement(agreement: Content) {
        val fragment = fragmentFactory.contentPresenterFragment(
            agreement,
            "card_settings_legal_cardholder_agreement_title".localized(),
            CONTENT_PRESENTER_TAG
        )
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun onCloseTapped() = popFragment()
}
