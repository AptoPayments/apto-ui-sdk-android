package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.repository.LocalCardDetailsRepository

internal class ClearCardDetailsUseCase(private val repo: LocalCardDetailsRepository) : UseCaseWithoutParams<Unit>() {
    override fun run(): Either<Failure, Unit> {
        repo.clear()
        return Either.Right(Unit)
    }
}
