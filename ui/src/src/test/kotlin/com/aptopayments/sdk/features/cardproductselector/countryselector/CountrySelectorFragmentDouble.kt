package com.aptopayments.sdk.features.cardproductselector.countryselector

import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.selectcountry.CountrySelectorContract

internal class CountrySelectorFragmentDouble(override var delegate: CountrySelectorContract.Delegate?) :
        BaseFragment(),
        CountrySelectorContract.View
{
    override fun layoutId(): Int = 0
    override fun backgroundColor() = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
