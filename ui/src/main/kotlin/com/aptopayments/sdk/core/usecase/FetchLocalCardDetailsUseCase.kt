package com.aptopayments.sdk.core.usecase

import androidx.lifecycle.LiveData
import com.aptopayments.mobile.data.card.CardDetails
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.repository.LocalCardDetailsRepository

internal class FetchLocalCardDetailsUseCase(private val repository: LocalCardDetailsRepository) :
    UseCaseWithoutParams<LiveData<CardDetails?>>() {

    override fun run(): Either<Failure, LiveData<CardDetails?>> = Either.Right(repository.getCardDetailsLiveData())
}
