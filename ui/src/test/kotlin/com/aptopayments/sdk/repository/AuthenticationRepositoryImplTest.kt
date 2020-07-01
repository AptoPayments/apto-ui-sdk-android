package com.aptopayments.sdk.repository

import android.content.SharedPreferences
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.UnitTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.mockito.Mock

class AuthenticationRepositoryImplTest : UnitTest() {

    @Mock
    private lateinit var aptoPlatform: AptoPlatformProtocol

    @Mock
    private lateinit var sharedPref: SharedPreferences

    @Test
    fun `when its created then it subscribes to listener`() {
        AuthenticationRepositoryImpl(sharedPref, aptoPlatform)

        verify(aptoPlatform).subscribeSessionInvalidListener(any(), any())
    }
}
