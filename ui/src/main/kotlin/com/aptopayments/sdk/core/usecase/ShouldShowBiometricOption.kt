package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.features.biometric.BiometricWrapper

internal class ShouldShowBiometricOption(
    private val biometricWrapper: BiometricWrapper,
    private val uiSdkProtocol: AptoUiSdkProtocol
) : UseCaseWithoutParams<Boolean>() {

    override fun run(): Either<Failure, Boolean> {
        return Either.Right(biometricWrapper.canAskBiometric() && isSecurityAvailable())
    }

    private fun isSecurityAvailable() =
        uiSdkProtocol.cardOptions.authenticateOnStartup() || uiSdkProtocol.cardOptions.authenticateWithPINOnPCI()
}
