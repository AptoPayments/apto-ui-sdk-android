package com.aptopayments.sdk.repository

import com.aptopayments.core.data.card.CardDetails
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.core.platform.AptoPlatform
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
