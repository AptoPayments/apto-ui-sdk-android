package com.aptopayments.sdk.core.usecase

import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.repository.AuthenticationRepository

internal class SavePinUseCase(private val authenticationRepository: AuthenticationRepository) : UseCase<Unit, String>() {

    override fun run(params: String): Either<Failure, Unit> {
        authenticationRepository.setPin(params)
        return Either.Right(Unit)
    }

}
