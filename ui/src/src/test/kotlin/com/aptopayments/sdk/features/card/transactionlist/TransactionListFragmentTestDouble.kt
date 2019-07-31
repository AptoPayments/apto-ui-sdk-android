package com.aptopayments.sdk.features.card.transactionlist

import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.card.transactionlist.TransactionListContract.Delegate

internal class TransactionListFragmentTestDouble : BaseFragment(), TransactionListContract.View {
    override var delegate: Delegate? = null

    override fun layoutId(): Int = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
