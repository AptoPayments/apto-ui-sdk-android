package com.aptopayments.sdk.features.auth

import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.auth.inputphone.InputPhoneContract

internal class AuthInputPhoneFragmentDouble(override var delegate: InputPhoneContract.Delegate?) :
    BaseFragment(),
    InputPhoneContract.View {

    override fun layoutId() = 0
    override fun backgroundColor() = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
