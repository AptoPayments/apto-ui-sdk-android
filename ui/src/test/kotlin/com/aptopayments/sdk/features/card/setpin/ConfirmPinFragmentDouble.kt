package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.sdk.core.platform.BaseFragment

internal class ConfirmPinFragmentDouble(override var delegate: ConfirmCardPinContract.Delegate?) :
    BaseFragment(),
    ConfirmCardPinContract.View {
    override fun layoutId(): Int = 0
    override fun backgroundColor() = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
