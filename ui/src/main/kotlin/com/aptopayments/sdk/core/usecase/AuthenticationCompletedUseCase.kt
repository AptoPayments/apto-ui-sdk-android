package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.sdk.repository.AuthenticationRepository

internal class AuthenticationCompletedUseCase(
    private val authenticationRepo: AuthenticationRepository
) : UseCaseWithoutParams<Unit>() {

    override fun run(): Either<Failure, Unit> {
        authenticationRepo.saveAuthenticatedCorrectly()
        return Unit.right()
    }
}
