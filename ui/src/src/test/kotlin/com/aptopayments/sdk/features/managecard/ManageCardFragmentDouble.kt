package com.aptopayments.sdk.features.managecard

import com.aptopayments.sdk.core.platform.BaseFragment

internal class ManageCardFragmentDouble(override var delegate: ManageCardContract.Delegate?) :
    BaseFragment(),
    ManageCardContract.View {
    override fun refreshTransactions() {}
    override fun refreshCardData() {}
    override fun refreshBalance() {}
    override fun layoutId(): Int = 0
    override fun backgroundColor() = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
