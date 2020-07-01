package com.aptopayments.sdk.core.platform

import com.aptopayments.mobile.platform.AptoPlatform

interface AuthStateProvider {
    fun userTokenPresent(): Boolean
}

class AuthStateProviderImpl : AuthStateProvider {
    override fun userTokenPresent(): Boolean = AptoPlatform.userTokenPresent()
}
