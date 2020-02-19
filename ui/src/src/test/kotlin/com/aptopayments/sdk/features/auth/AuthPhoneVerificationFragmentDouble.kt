package com.aptopayments.sdk.features.auth

import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.auth.verification.PhoneVerificationContract

internal class AuthPhoneVerificationFragmentDouble(override var delegate: PhoneVerificationContract.Delegate?) :
        BaseFragment(),
        PhoneVerificationContract.View
{
    override fun layoutId(): Int { return 0 }
    override fun backgroundColor() = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
