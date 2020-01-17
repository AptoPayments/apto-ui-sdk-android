package com.aptopayments.sdk.core.usecase

import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol

internal class ForgotPinUseCase(private val uiSdk: AptoUiSdkProtocol) : UseCaseWithoutParams<Unit>() {
    override fun run(): Either<Failure, Unit> {
        uiSdk.logout()
        return Either.Right(Unit)
    }
}
