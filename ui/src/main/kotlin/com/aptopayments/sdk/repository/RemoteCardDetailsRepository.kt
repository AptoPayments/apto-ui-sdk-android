package com.aptopayments.sdk.repository

import com.aptopayments.mobile.data.card.CardDetails
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatform
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface RemoteCardDetailsRepository {
    suspend fun fetch(cardId: String): Either<Failure, CardDetails>
}

class RemoteCardDetailsRepositoryImpl : RemoteCardDetailsRepository {
    override suspend fun fetch(cardId: String): Either<Failure, CardDetails> = suspendCoroutine { cont ->
        AptoPlatform.fetchCardDetails(cardId) { cont.resume(it) }
    }
}
