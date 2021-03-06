package com.aptopayments.sdk.features.card.transactionlist

import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.Either.Right
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.transactiondetails.TransactionDetailsContract

private const val TRANSACTION_LIST_TAG = "TransactionListFragment"
private const val TRANSACTION_DETAILS_TAG = "TransactionDetailsFragment"

internal class TransactionListFlow(
    private val cardId: String,
    private val config: TransactionListConfig,
    private val onBack: () -> Unit
) : Flow(), TransactionListContract.Delegate, TransactionDetailsContract.Delegate {
    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = fragmentFactory.transactionListFragment(cardId, config, TRANSACTION_LIST_TAG)
        fragment.delegate = this
        setStartElement(fragment as FlowPresentable)
        onInitComplete(Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(TRANSACTION_LIST_TAG) as? TransactionListContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onBackPressed() = onBack()

    override fun onTransactionTapped(transaction: Transaction) {
        val fragment = fragmentFactory.transactionDetailsFragment(transaction, TRANSACTION_DETAILS_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun onTransactionDetailsBackPressed() = popFragment()
}
