package com.aptopayments.sdk.features.managecard

import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.card.waitlist.WaitlistContract

internal class WaitlistFragmentDouble(override var delegate: WaitlistContract.Delegate?) :
    BaseFragment(),
    WaitlistContract.View {
    override fun layoutId(): Int = 0
    override fun backgroundColor() = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
