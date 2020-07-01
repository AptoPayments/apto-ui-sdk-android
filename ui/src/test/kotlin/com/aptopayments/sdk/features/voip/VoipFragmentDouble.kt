package com.aptopayments.sdk.features.voip

import com.aptopayments.sdk.core.platform.BaseFragment

internal class VoipFragmentDouble(override var delegate: VoipContract.Delegate?) :
    BaseFragment(),
    VoipContract.View {
    override fun layoutId(): Int = 0
    override fun backgroundColor() = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
