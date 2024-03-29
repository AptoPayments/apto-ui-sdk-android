package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.features.managecard.CardOptions.PCIAuthType
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.features.biometric.BiometricWrapper
import com.aptopayments.sdk.utils.shouldBeRightAndEqualTo
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ShouldShowBiometricOptionTest {

    private val cardOptions: CardOptions = mock()
    private val biometricWrapper: BiometricWrapper = mock()
    private val uiSdkProtocol: AptoUiSdkProtocol = mock()

    lateinit var sut: ShouldShowBiometricOption

    @BeforeEach
    fun before() {
        sut = ShouldShowBiometricOption(biometricWrapper, uiSdkProtocol)
    }

    @Test
    fun `when there are no biometrics then option is not shown`() {
        configureWrapper(false)

        val result = sut()

        result.shouldBeRightAndEqualTo(false)
    }

    @Test
    fun `when device support biometrics and we should authenticate on startup option is shown`() {
        configureWrapper(true)
        configureCardOptions(true, PCIAuthType.BIOMETRICS)

        val result = sut()

        result.shouldBeRightAndEqualTo(true)
    }

    @Test
    fun `when device support biometrics and we should authenticate on pci option is shown`() {
        configureWrapper(true)
        configureCardOptions(false, PCIAuthType.PIN_OR_BIOMETRICS)

        val result = sut()

        result.shouldBeRightAndEqualTo(true)
    }

    @Test
    fun `when device support biometrics and we should authenticate both option is shown`() {
        configureWrapper(true)
        configureCardOptions(true, PCIAuthType.PIN_OR_BIOMETRICS)

        val result = sut()

        result.shouldBeRightAndEqualTo(true)
    }

    private fun configureWrapper(canAskBiometric: Boolean) {
        whenever(biometricWrapper.canAskBiometric()).thenReturn(canAskBiometric)
    }

    private fun configureCardOptions(authenticateOnStartup: Boolean, pciAuthType: PCIAuthType) {
        whenever(uiSdkProtocol.cardOptions).thenReturn(cardOptions)
        whenever(cardOptions.authenticateOnStartup()).thenReturn(authenticateOnStartup)
        whenever(cardOptions.authenticatePCI()).thenReturn(pciAuthType)
    }
}
