package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.repository.AuthenticationRepository
import com.nhaarman.mockitokotlin2.given
import org.amshove.kluent.`should be`
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

internal class ShouldAuthenticateWithPINOnPCIUseCaseTest : AndroidTest() {

    @Mock
    private lateinit var authenticationRepo: AuthenticationRepository
    lateinit var sut: ShouldAuthenticateWithPINOnPCIUseCase

    @Before
    fun configureKoin() {
        sut = ShouldAuthenticateWithPINOnPCIUseCase(authenticationRepo)
    }

    @Test
    fun `when featureFlag and authTimeInvalid then Needs Authentication`() {
        checkCase(
            authWithPINOnPCI = true,
            isAuthTimeValid = true,
            biometricsEnabled = false,
            expectedResult = true
        )
    }

    @Test
    fun `when featureFlag and authTime valid then Doesn't Needs Authentication`() {
        checkCase(
            authWithPINOnPCI = true,
            isAuthTimeValid = false,
            biometricsEnabled = false,
            expectedResult = false
        )
    }

    @Test
    fun `when featureFlag off, biometrics disabled then Doesn't Needs Authentication`() {
        checkCase(
            authWithPINOnPCI = false,
            isAuthTimeValid = false,
            biometricsEnabled = false,
            expectedResult = false
        )
        checkCase(
            authWithPINOnPCI = false,
            isAuthTimeValid = true,
            biometricsEnabled = false,
            expectedResult = false
        )
    }

    @Test
    fun `when featureFlag off, biometrics enabled authTime Valid then Doesn't Needs Authentication`() {
        checkCase(
            authWithPINOnPCI = false,
            isAuthTimeValid = false,
            biometricsEnabled = true,
            expectedResult = true
        )
    }

    @Test
    fun `when featureFlag off, biometrics enabled authTime Invalid then Needs Authentication`() {
        checkCase(
            authWithPINOnPCI = false,
            isAuthTimeValid = true,
            biometricsEnabled = true,
            expectedResult = true
        )
    }

    private fun checkCase(
        authWithPINOnPCI: Boolean,
        isAuthTimeValid: Boolean,
        biometricsEnabled: Boolean,
        expectedResult: Boolean
    ) {
        AptoUiSdk.cardOptions = CardOptions(authenticateWithPINOnPCI = authWithPINOnPCI)
        given(authenticationRepo.isAuthTimeInvalid()).willReturn(isAuthTimeValid)
        given(authenticationRepo.isBiometricsEnabledByUser()).willReturn(biometricsEnabled)

        val result = sut()

        result.isRight `should be` true
        result.either({}, { value -> value `should be` expectedResult })
    }
}
