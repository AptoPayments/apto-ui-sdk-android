package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.repository.AuthenticationRepository

internal class ShouldAuthenticateWithPINOnPCIUseCase(
    private val authenticationRepo: AuthenticationRepository
) : UseCaseWithoutParams<Boolean>() {

    override fun run(): Either<Failure, Boolean> {
        return if (AptoUiSdk.cardOptions.authenticateWithPINOnPCI()) {
            Either.Right(authenticationRepo.isAuthTimeInvalid())
        } else {
            Either.Right(authenticationRepo.isBiometricsEnabledByUser())
        }
    }
}
