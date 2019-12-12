package com.aptopayments.sdk.core.usecase

import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.repository.AuthenticationRepository

internal class VerifyPinUseCase(private val authenticationRepository: AuthenticationRepository) :
    UseCase<Boolean, String>() {
    override fun run(params: String): Either<Nothing, Boolean> {
        val result = authenticationRepository.getPin() == params
        saveAuthIfCorrect(result)
        return Either.Right(result)
    }

    private fun saveAuthIfCorrect(result: Boolean) {
        if (result) {
            authenticationRepository.saveAuthenticationTime()
        }
    }
}
