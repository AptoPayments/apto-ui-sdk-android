package com.aptopayments.sdk.features.issuecard

import com.aptopayments.sdk.core.platform.BaseFragment

internal class IssueCardFragmentDouble(override var delegate: IssueCardContract.Delegate?) :
        BaseFragment(),
        IssueCardContract.View
{
    override fun issueCard() {}
    override fun layoutId(): Int = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
