package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
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
