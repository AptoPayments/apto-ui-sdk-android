package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.features.managecard.CardOptions.PCIAuthType
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.repository.AuthenticationRepository
import com.aptopayments.sdk.utils.shouldBeRightAndEqualTo
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ShouldCreatePasscodeUseCaseTest {

    private val authenticationRepo: AuthenticationRepository = mock()
    lateinit var sut: ShouldCreatePasscodeUseCase

    @BeforeEach
    fun before() {
        sut = ShouldCreatePasscodeUseCase(authenticationRepo)
    }

    @Test
    fun whenPinSetNoPinNeed() {
        checkCase(pinSet = true, pinOnStartup = true, pciAuth = PCIAuthType.NONE, resultExpected = false)
        checkCase(pinSet = true, pinOnStartup = true, pciAuth = PCIAuthType.BIOMETRICS, resultExpected = false)
        checkCase(pinSet = true, pinOnStartup = false, pciAuth = PCIAuthType.PIN_OR_BIOMETRICS, resultExpected = false)
        checkCase(pinSet = true, pinOnStartup = true, pciAuth = PCIAuthType.PIN_OR_BIOMETRICS, resultExpected = false)
    }

    @Test
    fun whenNoPinSetThenNeedsIfAnyFeatureFlagIsOnPin() {
        checkCase(pinSet = false, pinOnStartup = false, pciAuth = PCIAuthType.NONE, resultExpected = false)
        checkCase(pinSet = false, pinOnStartup = true, pciAuth = PCIAuthType.NONE, resultExpected = true)
        checkCase(pinSet = false, pinOnStartup = false, pciAuth = PCIAuthType.BIOMETRICS, resultExpected = false)
        checkCase(pinSet = false, pinOnStartup = true, pciAuth = PCIAuthType.BIOMETRICS, resultExpected = true)
        checkCase(pinSet = false, pinOnStartup = false, pciAuth = PCIAuthType.PIN_OR_BIOMETRICS, resultExpected = true)
        checkCase(pinSet = false, pinOnStartup = true, pciAuth = PCIAuthType.PIN_OR_BIOMETRICS, resultExpected = true)
    }

    private fun checkCase(
        pinSet: Boolean,
        pinOnStartup: Boolean,
        pciAuth: PCIAuthType,
        resultExpected: Boolean
    ) {
        whenever(authenticationRepo.isPasscodeSet()).thenReturn(pinSet)
        configureFlags(startup = pinOnStartup, pci = pciAuth)
        val result = sut()

        result.shouldBeRightAndEqualTo(resultExpected)
    }

    private fun configureFlags(startup: Boolean, pci: PCIAuthType) {
        AptoUiSdk.cardOptions =
            CardOptions(authenticateOnStartup = startup, authenticateOnPCI = pci)
    }
}
