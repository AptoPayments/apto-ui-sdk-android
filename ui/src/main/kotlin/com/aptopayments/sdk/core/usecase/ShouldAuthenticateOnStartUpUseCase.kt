package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.core.platform.AuthStateProvider
import com.aptopayments.sdk.repository.AuthenticationRepository

internal class ShouldAuthenticateOnStartUpUseCase(
    private val authStateProviderImpl: AuthStateProvider,
    private val authenticationRepo: AuthenticationRepository
) : UseCaseWithoutParams<Boolean>() {

    override fun run(): Either<Failure, Boolean> {
        return Either.Right(
            AptoUiSdk.cardOptions.authenticateOnStartup() &&
                authStateProviderImpl.userTokenPresent() &&
                (authenticationRepo.isAuthenticationNeedSaved() || authenticationRepo.isAuthTimeInvalid()) &&
                authenticationRepo.isPasscodeSet()
        )
    }
}
