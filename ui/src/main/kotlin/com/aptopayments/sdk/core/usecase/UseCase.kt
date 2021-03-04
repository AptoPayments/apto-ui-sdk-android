package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

    operator fun invoke(params: Params, onResult: (Either<Failure, Type>) -> Unit = {}) {
        GlobalScope.launch {
            val result = run(params)
            launch(Dispatchers.Main) {
                onResult(result)
            }
        }
    }
}

internal abstract class UseCaseAsyncWithoutParams<out Type> where Type : Any {
    abstract suspend fun run(): Either<Failure, Type>

    suspend operator fun invoke(): Either<Failure, Type> {
        return run()
    }

    operator fun invoke(onResult: (Either<Failure, Type>) -> Unit = {}) {
        GlobalScope.launch {
            val result = run()
            launch(Dispatchers.Main) {
                onResult(result)
            }
        }
    }
}
