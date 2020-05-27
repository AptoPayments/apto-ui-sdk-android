package com.aptopayments.sdk.core.usecase

import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.repository.AuthenticationRepository

internal class BiometricsAuthCorrectUseCase(
    private val authenticationRepository: AuthenticationRepository
) : UseCaseWithoutParams<Unit>() {

    override fun run(): Either<Failure, Unit> {
        authenticationRepository.saveAuthenticationTime()
        return Either.Right(Unit)
    }
}
