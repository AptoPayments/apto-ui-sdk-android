package com.aptopayments.sdk.features.auth

import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.auth.verification.EmailVerificationContract

internal class AuthVerifyEmailFragmentDouble(override var delegate: EmailVerificationContract.Delegate?) :
        BaseFragment(),
        EmailVerificationContract.View
{
    override fun layoutId(): Int { return 0 }
    override fun backgroundColor() = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
