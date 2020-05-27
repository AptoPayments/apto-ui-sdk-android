package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.sdk.core.platform.BaseFragment

internal class ConfirmPinFragmentDouble(override var delegate: ConfirmPinContract.Delegate?) :
    BaseFragment(),
    ConfirmPinContract.View {
    override fun layoutId(): Int = 0
    override fun backgroundColor() = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
