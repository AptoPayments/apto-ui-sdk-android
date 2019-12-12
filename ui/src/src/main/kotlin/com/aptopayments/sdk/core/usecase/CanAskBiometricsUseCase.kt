package com.aptopayments.sdk.core.usecase

import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.features.biometric.BiometricWrapper
import com.aptopayments.sdk.repository.AuthenticationRepository

internal class CanAskBiometricsUseCase(
    private val authenticationRepository: AuthenticationRepository,
    private val wrapper: BiometricWrapper
) : UseCaseWithoutParams<Boolean>() {

    override fun run(): Either<Failure, Boolean> {
        return Either.Right(authenticationRepository.isBiometricsEnabledByUser() && wrapper.canAskBiometric())
    }

}
