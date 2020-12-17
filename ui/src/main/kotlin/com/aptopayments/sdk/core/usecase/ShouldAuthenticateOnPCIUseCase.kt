package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.features.managecard.CardOptions.PCIAuthType
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.repository.AuthenticationRepository

internal class ShouldAuthenticateOnPCIUseCase(
    private val authenticationRepo: AuthenticationRepository
) : UseCaseWithoutParams<Boolean>() {

    override fun run(): Either<Failure, Boolean> {
        return when (AptoUiSdk.cardOptions.authenticatePCI()) {
            PCIAuthType.PIN_OR_BIOMETRICS -> Either.Right(authenticationRepo.isAuthTimeInvalid())
            PCIAuthType.BIOMETRICS -> Either.Right(authenticationRepo.isBiometricsEnabledByUser())
            else -> false.right()
        }
    }
}
