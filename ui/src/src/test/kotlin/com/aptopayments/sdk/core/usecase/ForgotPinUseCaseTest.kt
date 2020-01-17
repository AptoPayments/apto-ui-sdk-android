package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

internal class ForgotPinUseCaseTest : UnitTest() {

    @Mock
    private lateinit var uiSdk: AptoUiSdkProtocol

    lateinit var sut: ForgotPinUseCase

    @Before
    fun configure() {
        sut = ForgotPinUseCase(uiSdk)
    }

    @Test
    fun `when Forgot Pin then logout called`() {
        sut()

        verify(uiSdk).logout()
    }
}
