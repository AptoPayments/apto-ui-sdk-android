package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ForgotPinUseCaseTest {

    private val uiSdk: AptoUiSdkProtocol = mock()

    lateinit var sut: ForgotPinUseCase

    @BeforeEach
    fun configure() {
        sut = ForgotPinUseCase(uiSdk)
    }

    @Test
    fun `when Forgot Pin then logout called`() {
        sut()

        verify(uiSdk).logout()
    }
}
