package com.aptopayments.sdk.core.usecase

import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either

internal abstract class UseCase<out Type, in Params> where Type : Any {

    abstract fun run(params: Params): Either<Failure, Type>

    operator fun invoke(params: Params): Either<Failure, Type> {
        return run(params)
    }
}

internal abstract class UseCaseWithoutParams<out Type> where Type : Any {

    abstract fun run(): Either<Failure, Type>

    operator fun invoke(): Either<Failure, Type> {
        return run()
    }
}

internal abstract class UseCaseAsync<out Type, in Params> where Type : Any {
    abstract suspend fun run(params: Params): Either<Failure, Type>

    suspend operator fun invoke(params: Params): Either<Failure, Type> {
        return run(params)
    }
}
