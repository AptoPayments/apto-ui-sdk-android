package com.aptopayments.sdk.core.usecase

import com.aptopayments.core.features.managecard.CardOptions
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.repository.AuthenticationRepository
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.`should be`
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

internal class ShouldCreatePasscodeUseCaseTest : UnitTest() {

    @Mock
    private lateinit var authenticationRepo: AuthenticationRepository
    lateinit var sut: ShouldCreatePasscodeUseCase

    @Before
    fun before() {
        sut = ShouldCreatePasscodeUseCase(authenticationRepo)
    }

    @Test
    fun whenPinSetNoPinNeed() {
        checkCase(pinSet = true, pinOnStartup = true, pinOnPCI = false, resultExpected = false)
        checkCase(pinSet = true, pinOnStartup = false, pinOnPCI = true, resultExpected = false)
        checkCase(pinSet = true, pinOnStartup = true, pinOnPCI = true, resultExpected = false)
    }

    @Test
    fun whenNoPinSetThenNeedsIfAnyFeatureFlagIsOn() {
        checkCase(pinSet = false, pinOnStartup = false, pinOnPCI = false, resultExpected = false)
        checkCase(pinSet = false, pinOnStartup = true, pinOnPCI = false, resultExpected = true)
        checkCase(pinSet = false, pinOnStartup = false, pinOnPCI = true, resultExpected = true)
        checkCase(pinSet = false, pinOnStartup = true, pinOnPCI = true, resultExpected = true)
    }

    private fun checkCase(
        pinSet: Boolean,
        pinOnStartup: Boolean,
        pinOnPCI: Boolean,
        resultExpected: Boolean
    ) {
        whenever(authenticationRepo.isPasscodeSet()).thenReturn(pinSet)
        configureFlags(startup = pinOnStartup, pci = pinOnPCI)
        val result = sut()

        result.isRight `should be` true
        result.either({}, { value -> value `should be` resultExpected })
    }

    private fun configureFlags(startup: Boolean, pci: Boolean) {
        AptoUiSdk.cardOptions = CardOptions(authenticateOnStartup = startup, authenticateWithPINOnPCI = pci)
    }

}
