package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol

internal class ForgotPinUseCase(private val uiSdk: AptoUiSdkProtocol) : UseCaseWithoutParams<Unit>() {
    override fun run(): Either<Failure, Unit> {
        uiSdk.logout()
        return Either.Right(Unit)
    }
}
