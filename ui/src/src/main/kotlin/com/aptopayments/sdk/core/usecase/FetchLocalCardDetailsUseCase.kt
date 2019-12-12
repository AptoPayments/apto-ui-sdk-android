package com.aptopayments.sdk.core.usecase

import androidx.lifecycle.LiveData
import com.aptopayments.core.data.card.CardDetails
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.repository.LocalCardDetailsRepository

internal class FetchLocalCardDetailsUseCase(private val repository: LocalCardDetailsRepository) :
    UseCaseWithoutParams<LiveData<CardDetails?>>() {

    override fun run(): Either<Failure, LiveData<CardDetails?>> = Either.Right(repository.getCardDetailsLiveData())

}
