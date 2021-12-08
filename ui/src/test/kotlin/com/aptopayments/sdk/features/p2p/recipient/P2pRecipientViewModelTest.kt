package com.aptopayments.sdk.features.p2p.recipient

import com.aptopayments.mobile.data.PhoneNumber
import com.aptopayments.mobile.data.transfermoney.CardHolderData
import com.aptopayments.mobile.data.transfermoney.CardHolderName
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.exception.server.ErrorP2PSelfRecipient
import com.aptopayments.mobile.exception.server.ErrorRecipientNotFound
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.left
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.*

private const val PHONE_NUMBER = "1243"
private const val COUNTRY_CODE = "1"
private const val EMAIL = "a@a.com"

@ExtendWith(InstantExecutorExtension::class)
internal class P2pRecipientViewModelTest {

    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val sut = P2pRecipientViewModel(0, aptoPlatform)

    private val phone = PhoneNumber(COUNTRY_CODE, PHONE_NUMBER)
    private val cardHolderData = CardHolderData(CardHolderName("name", "surname"), "123")

    @Test
    internal fun `given a correct PhoneNumber when is found in backend then state is correct`() {
        configureP2pFindRecipient(phone, null, cardHolderData.right())

        sut.onPhoneCountryChanged(COUNTRY_CODE)
        sut.onPhoneNumberChanged(PHONE_NUMBER, true)

        val state = sut.state.getOrAwaitValue()
        val cardholder = sut.cardholder.getOrAwaitValue()

        assertEquals(RecipientError.NO_UI_ERROR, state.error)
        assertNotNull(cardholder)
        assertEquals(cardHolderData, cardholder.data)
        assertTrue(state.showContinueButton)
    }

    @Test
    internal fun `given a correct Email when is found in backend then state is correct`() {
        configureP2pFindRecipient(null, EMAIL, cardHolderData.right())

        sut.onEmailChanged(EMAIL)

        val state = sut.state.getOrAwaitValue()
        val cardholder = sut.cardholder.getOrAwaitValue()

        assertEquals(RecipientError.NO_UI_ERROR, state.error)
        assertNotNull(cardholder)
        assertEquals(cardHolderData, cardholder.data)
        assertTrue(state.showContinueButton)
    }

    @Test
    internal fun `given a correct PhoneNumber when not found in backend then state is correct`() {
        configureP2pFindRecipient(phone, null, ErrorRecipientNotFound().left())

        sut.onPhoneCountryChanged(COUNTRY_CODE)
        sut.onPhoneNumberChanged(PHONE_NUMBER, true)

        val state = sut.state.getOrAwaitValue()
        val cardholder = sut.cardholder.getOrAwaitValue()

        assertEquals(RecipientError.NOT_FOUND, state.error)
        assertNull(cardholder)
        assertFalse(state.showContinueButton)
    }

    @Test
    internal fun `given my phone number when call the api then state is correct`() {
        configureP2pFindRecipient(phone, null, ErrorP2PSelfRecipient().left())

        sut.onPhoneCountryChanged(COUNTRY_CODE)
        sut.onPhoneNumberChanged(PHONE_NUMBER, true)

        val state = sut.state.getOrAwaitValue()
        val cardholder = sut.cardholder.getOrAwaitValue()

        assertEquals(RecipientError.SELF_RECIPIENT, state.error)
        assertNull(cardholder)
        assertFalse(state.showContinueButton)
    }

    @Test
    internal fun `given a correct phone number when generic server error then state is correct`() {
        configureP2pFindRecipient(phone, null, Failure.ServerError(0).left())

        sut.onPhoneCountryChanged(COUNTRY_CODE)
        sut.onPhoneNumberChanged(PHONE_NUMBER, true)

        val state = sut.state.getOrAwaitValue()
        val cardholder = sut.cardholder.getOrAwaitValue()

        assertEquals(RecipientError.NO_UI_ERROR, state.error)
        assertNull(cardholder)
        assertFalse(state.showContinueButton)
    }

    @Suppress("UNCHECKED_CAST")
    private fun configureP2pFindRecipient(
        phone: PhoneNumber?,
        email: String?,
        result: Either<Failure, CardHolderData>
    ) {
        whenever(
            aptoPlatform.p2pFindRecipient(
                eq(phone),
                eq(email),
                any()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, CardHolderData>) -> Unit).invoke(
                result
            )
        }
    }
}
