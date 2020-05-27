package com.aptopayments.sdk.core.usecase

import com.aptopayments.core.exception.Failure
import com.aptopayments.core.features.managecard.CardOptions
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.features.biometric.BiometricWrapper
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

internal class ShouldShowBiometricOptionTest : UnitTest() {

    @Mock
    private lateinit var cardOptions: CardOptions

    @Mock
    private lateinit var biometricWrapper: BiometricWrapper

    @Mock
    private lateinit var uiSdkProtocol: AptoUiSdkProtocol

    lateinit var sut: ShouldShowBiometricOption

    @Before
    fun before() {
        sut = ShouldShowBiometricOption(biometricWrapper, uiSdkProtocol)
    }

    @Test
    fun `when there are no biometrics then option is not shown`() {
        configureWrapper(false)

        val result = sut()

        assertRightEitherIsEqualTo(result, false)
    }

    @Test
    fun `when device support biometrics and we should authenticate on startup option is shown`() {
        configureWrapper(true)
        configureCardOptions(true, false)

        val result = sut()

        assertRightEitherIsEqualTo(result, true)
    }

    @Test
    fun `when device support biometrics and we should authenticate on pci option is shown`() {
        configureWrapper(true)
        configureCardOptions(false, true)

        val result = sut()

        assertRightEitherIsEqualTo(result, true)
    }

    @Test
    fun `when device support biometrics and we should authenticate both option is shown`() {
        configureWrapper(true)
        configureCardOptions(true, true)

        val result = sut()

        assertRightEitherIsEqualTo(result, true)
    }

    fun configureWrapper(canAskBiometric: Boolean) {
        whenever(biometricWrapper.canAskBiometric()).thenReturn(canAskBiometric)
    }

    fun configureCardOptions(authenticateOnStartup: Boolean, authenticateOnPCI: Boolean) {
        whenever(uiSdkProtocol.cardOptions).thenReturn(cardOptions)
        whenever(cardOptions.authenticateOnStartup()).thenReturn(authenticateOnStartup)
        whenever(cardOptions.authenticateWithPINOnPCI()).thenReturn(authenticateOnPCI)
    }

    private fun assertRightEitherIsEqualTo(result: Either<Failure, Any>, rightValue: Boolean) {
        result shouldBeInstanceOf Either::class.java
        result.isRight shouldEqual true
        result.either({}, { it shouldBe rightValue })
    }
}
