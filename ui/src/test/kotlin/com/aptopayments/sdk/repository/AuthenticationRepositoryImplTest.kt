package com.aptopayments.sdk.repository

import android.content.SharedPreferences
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.Test

private const val NEED_AUTHENTICATION = "NEED_AUTHENTICATION"

class AuthenticationRepositoryImplTest {

    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val sharedPref: SharedPreferences = mock()
    private val editor: SharedPreferences.Editor = mock()

    private val sut = AuthenticationRepositoryImpl(sharedPref, aptoPlatform)

    @Test
    fun `when its created then it subscribes to listener`() {
        verify(aptoPlatform).subscribeSessionInvalidListener(any(), any())
    }

    @Test
    fun `when saveNeedToAuthenticate then true is saved immediately`() {
        configureSharedPref()

        sut.saveNeedToAuthenticate()

        verify(editor).putBoolean(NEED_AUTHENTICATION, true)
        verify(editor).commit()
    }

    @Test
    fun `when saveAuthenticatedCorrectly then false is saved immediately`() {
        configureSharedPref()

        sut.saveAuthenticatedCorrectly()

        verify(editor).putBoolean(NEED_AUTHENTICATION, false)
        verify(editor).commit()
    }

    private fun configureSharedPref() {
        whenever(sharedPref.edit()).thenReturn(editor)
        whenever(editor.putBoolean(any(), any())).thenReturn(editor)
    }
}
