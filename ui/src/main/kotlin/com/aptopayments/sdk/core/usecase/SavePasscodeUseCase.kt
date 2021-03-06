package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.repository.AuthenticationRepository

internal class SavePasscodeUseCase(private val authenticationRepository: AuthenticationRepository) : UseCase<Unit, String>() {

    override fun run(params: String): Either<Failure, Unit> {
        authenticationRepository.setPasscode(params)
        return Either.Right(Unit)
    }
}
