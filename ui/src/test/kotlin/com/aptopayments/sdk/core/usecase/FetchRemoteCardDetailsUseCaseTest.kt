package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.data.card.CardDetails
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.usecase.FetchRemoteCardDetailsUseCase.Params
import com.aptopayments.sdk.repository.LocalCardDetailsRepository
import com.aptopayments.sdk.repository.RemoteCardDetailsRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val CARD_ID = "1234"
private const val PAN = "1234567812345678"
private const val CVV = "123"
private const val EXPIRATION_DATE = "2020-10"

@ExperimentalCoroutinesApi
internal class FetchRemoteCardDetailsUseCaseTest : UnitTest() {

    @Mock
    private lateinit var localRepo: LocalCardDetailsRepository

    @Mock
    private lateinit var remoteRepo: RemoteCardDetailsRepository

    private val cardDetails = CardDetails(PAN, CVV, EXPIRATION_DATE)

    lateinit var sut: FetchRemoteCardDetailsUseCase

    @Before
    fun configure() {
        sut = FetchRemoteCardDetailsUseCase(localRepo, remoteRepo)
    }

    @Test
    fun `when Remote Fetch is correct then details are saved and returned`() = runBlockingTest {
        whenever(remoteRepo.fetch(any())).thenReturn(Either.Right(cardDetails))

        val result = sut(Params(CARD_ID))

        verify(localRepo).saveCardDetails(cardDetails)
        assertTrue(result.isRight)
        assertEquals((result as Either.Right).b, cardDetails)
    }

    @Test
    fun `when Remote Fetch is fails nothing is saved`() = runBlockingTest {
        whenever(remoteRepo.fetch(any())).thenReturn(Either.Left(Failure.ServerError(0)))

        val result = sut(Params(CARD_ID))

        verify(localRepo, times(0)).saveCardDetails(cardDetails)
        assertTrue(result.isLeft)
    }
}
