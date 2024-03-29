package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.features.managecard.CardOptions.PCIAuthType
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.repository.AuthenticationRepository
import com.aptopayments.sdk.utils.shouldBeRightAndEqualTo
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ShouldAuthenticateOnPCIUseCaseTest {

    private val authenticationRepo: AuthenticationRepository = mock()
    lateinit var sut: ShouldAuthenticateOnPCIUseCase

    @BeforeEach
    fun configureKoin() {
        sut = ShouldAuthenticateOnPCIUseCase(authenticationRepo)
    }

    @Test
    fun `when AuthType is PIN_OR_BIOMETRICS and authTimeInvalid then Needs Authentication`() {
        checkCase(
            authWithPINOnPCI = PCIAuthType.PIN_OR_BIOMETRICS,
            isAuthTimeValid = true,
            biometricsEnabled = false,
            expectedResult = true
        )
    }

    @Test
    fun `when AuthType is PIN_OR_BIOMETRICS and authTime valid then Doesn't Needs Authentication`() {
        checkCase(
            authWithPINOnPCI = PCIAuthType.PIN_OR_BIOMETRICS,
            isAuthTimeValid = false,
            biometricsEnabled = false,
            expectedResult = false
        )
    }

    @Test
    fun `when AuthType is BIOMETRICS, biometrics disabled then Doesn't Needs Authentication`() {
        checkCase(
            authWithPINOnPCI = PCIAuthType.BIOMETRICS,
            isAuthTimeValid = false,
            biometricsEnabled = false,
            expectedResult = false
        )
        checkCase(
            authWithPINOnPCI = PCIAuthType.BIOMETRICS,
            isAuthTimeValid = true,
            biometricsEnabled = false,
            expectedResult = false
        )
    }

    @Test
    fun `when AuthType is BIOMETRICS, biometrics enabled authTime Valid then Doesn't Needs Authentication`() {
        checkCase(
            authWithPINOnPCI = PCIAuthType.BIOMETRICS,
            isAuthTimeValid = false,
            biometricsEnabled = true,
            expectedResult = true
        )
    }

    @Test
    fun `when AuthType is BIOMETRICS, biometrics enabled authTime Invalid then Needs Authentication`() {
        checkCase(
            authWithPINOnPCI = PCIAuthType.BIOMETRICS,
            isAuthTimeValid = true,
            biometricsEnabled = true,
            expectedResult = true
        )
    }

    @Test
    fun `when AuthType is None then never needs Authentication`() {
        checkCase(
            authWithPINOnPCI = PCIAuthType.NONE,
            isAuthTimeValid = true,
            biometricsEnabled = true,
            expectedResult = false
        )
        checkCase(
            authWithPINOnPCI = PCIAuthType.NONE,
            isAuthTimeValid = false,
            biometricsEnabled = true,
            expectedResult = false
        )
        checkCase(
            authWithPINOnPCI = PCIAuthType.NONE,
            isAuthTimeValid = true,
            biometricsEnabled = false,
            expectedResult = false
        )
        checkCase(
            authWithPINOnPCI = PCIAuthType.NONE,
            isAuthTimeValid = false,
            biometricsEnabled = false,
            expectedResult = false
        )
    }

    private fun checkCase(
        authWithPINOnPCI: PCIAuthType,
        isAuthTimeValid: Boolean,
        biometricsEnabled: Boolean,
        expectedResult: Boolean
    ) {
        AptoUiSdk.cardOptions = CardOptions(authenticateOnPCI = authWithPINOnPCI)
        given(authenticationRepo.isAuthTimeInvalid()).willReturn(isAuthTimeValid)
        given(authenticationRepo.isBiometricsEnabledByUser()).willReturn(biometricsEnabled)

        val result = sut()

        result.shouldBeRightAndEqualTo(expectedResult)
    }
}
