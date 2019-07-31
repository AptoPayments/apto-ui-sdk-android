package com.aptopayments.sdk.features.card.transactionlist

import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.transactiondetails.TransactionDetailsContract

internal class TransactionDetailsFragmentTestDouble : BaseFragment(), TransactionDetailsContract.View {
    override var delegate: TransactionDetailsContract.Delegate? = null

    override fun layoutId(): Int = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
