package com.aptopayments.sdk.features.auth

import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.auth.birthdateverification.BirthdateVerificationContract

internal class AuthVerifyBirthdayFragmentDouble(override var delegate: BirthdateVerificationContract.Delegate?) :
        BaseFragment(),
        BirthdateVerificationContract.View
{
    override fun layoutId(): Int { return 0 }
    override fun setupViewModel() {}
    override fun setupUI() {}
}
