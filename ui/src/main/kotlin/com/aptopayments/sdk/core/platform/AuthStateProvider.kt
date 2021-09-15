package com.aptopayments.sdk.core.platform

import com.aptopayments.mobile.platform.AptoPlatform

internal interface AuthStateProvider {
    fun userTokenPresent(): Boolean
}

internal class AuthStateProviderImpl : AuthStateProvider {
    override fun userTokenPresent(): Boolean = AptoPlatform.userTokenPresent()
}
