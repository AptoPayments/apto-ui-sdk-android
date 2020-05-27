package com.aptopayments.sdk.features.auth

import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.auth.inputemail.InputEmailContract

internal class AuthInputEmailFragmentDouble(override var delegate: InputEmailContract.Delegate?) :
    BaseFragment(),
    InputEmailContract.View {

    override fun layoutId() = 0
    override fun backgroundColor() = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
