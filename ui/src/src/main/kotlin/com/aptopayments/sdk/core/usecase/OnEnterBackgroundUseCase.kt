package com.aptopayments.sdk.core.usecase

import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.core.platform.AuthStateProvider
import com.aptopayments.sdk.repository.AuthenticationRepository

internal class OnEnterBackgroundUseCase(
    private val authState: AuthStateProvider,
    private val authenticationRepo: AuthenticationRepository
) : UseCaseWithoutParams<Unit>() {

    override fun run(): Either<Failure, Unit> {
        if (authState.userTokenPresent()) {
            authenticationRepo.saveNeedToAuthenticate()
        }
        return Either.Right(Unit)
    }

}
