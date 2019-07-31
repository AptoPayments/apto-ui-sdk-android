package com.aptopayments.sdk.features.issuecard

import com.aptopayments.sdk.core.platform.BaseFragment

internal class IssueCardErrorFragmentDouble(override var delegate: IssueCardErrorContract.Delegate?) :
        BaseFragment(),
        IssueCardErrorContract.View
{
    override fun layoutId(): Int = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
