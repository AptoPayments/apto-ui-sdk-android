package com.aptopayments.sdk.core.usecase

import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.repository.AuthenticationRepository

internal class ShouldCreatePINUseCase(private val authenticationRepository: AuthenticationRepository) :
    UseCaseWithoutParams<Boolean>() {

    override fun run(): Either<Failure, Boolean> {
        return Either.Right(
            !authenticationRepository.isPinSet() &&
                    (AptoUiSdk.cardOptions.authenticateOnStartup() || AptoUiSdk.cardOptions.authenticateWithPINOnPCI())
        )
    }

}
