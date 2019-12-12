package com.aptopayments.sdk.core.usecase

import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.core.platform.AptoPlatform

internal class ForgotPinUseCase : UseCaseWithoutParams<Unit>() {
    override fun run(): Either<Failure, Unit> {
        AptoPlatform.logout()
        return Either.Right(Unit)
    }
}
