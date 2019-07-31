package com.aptopayments.sdk.features.managecard

import com.aptopayments.sdk.core.platform.BaseDialogFragment
import com.aptopayments.sdk.features.card.fundingsources.FundingSourceContract

internal class FundingSourcesDialogFragmentDouble(override var delegate: FundingSourceContract.Delegate?) :
        BaseDialogFragment(),
        FundingSourceContract.View
{
    override fun setUpUI() {}
    override fun setUpViewModel() {}
    override fun setUpListeners() {}
    override fun layoutId(): Int = 0
}
