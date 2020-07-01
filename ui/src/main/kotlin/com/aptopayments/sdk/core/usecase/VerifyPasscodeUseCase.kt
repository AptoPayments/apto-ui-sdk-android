package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.repository.AuthenticationRepository

internal class VerifyPasscodeUseCase(private val authenticationRepository: AuthenticationRepository) :
    UseCase<Boolean, String>() {
    override fun run(params: String): Either<Nothing, Boolean> {
        val result = authenticationRepository.getPasscode() == params
        saveAuthIfCorrect(result)
        return Either.Right(result)
    }

    private fun saveAuthIfCorrect(result: Boolean) {
        if (result) {
            authenticationRepository.saveAuthenticationTime()
        }
    }
}
