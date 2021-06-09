package com.aptopayments.sdk.features.loadfunds.paymentsources

import android.content.SharedPreferences
import com.aptopayments.mobile.data.paymentsources.Card
import com.aptopayments.mobile.data.paymentsources.NewCard
import com.aptopayments.mobile.data.paymentsources.PaymentSource
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.CoroutineDispatcherTest
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.utils.shouldBeRightAndEqualTo
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val ACCEPTED_ONBOARDING = "ADD_CARD_ACCEPTED_ONBOARDING"

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class)
class PaymentSourcesRepositoryTest : CoroutineDispatcherTest {

    override lateinit var dispatcher: TestCoroutineDispatcher

    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val editor: SharedPreferences.Editor = mock()
    private val sharedPreferences: SharedPreferences = mock()

    private val sut = PaymentSourcesRepository(aptoPlatform, sharedPreferences)

    @Test
    fun `when accepted onboarding then saved to sharedPref`() {
        configureSharedPref()

        sut.acceptedOnboarding()

        verify(editor).putBoolean(ACCEPTED_ONBOARDING, true)
        verify(editor).apply()
    }

    @Test
    fun `when hasAcceptedOnboarding then result is got from sharedPref`() {
        configureSharedPref()

        sut.hasAcceptedOnboarding()

        verify(sharedPreferences).getBoolean(ACCEPTED_ONBOARDING, false)
    }

    @Test
    fun `when repo without interaction then selectedPaymentSource is null`() = dispatcher.runBlockingTest {
        assertNull(sut.selectedPaymentSource.value)
    }

    @Test
    fun `when selectPaymentSourceLocally then value is updated`() = dispatcher.runBlockingTest {
        val card = TestDataProvider.providePaymentSourcesCard()

        sut.selectPaymentSourceLocally(card)

        assertEquals(card, sut.selectedPaymentSource.value)
    }

    @Test
    fun `when selectPaymentSourceLocally then no api-call is made`() = dispatcher.runBlockingTest {
        val card = TestDataProvider.providePaymentSourcesCard()

        sut.selectPaymentSourceLocally(card)

        verifyZeroInteractions(aptoPlatform)
    }

    @Test
    fun `when refreshSelectedPaymentSource then correct payment source returned in liveData`() = dispatcher.runBlockingTest {
        val card = TestDataProvider.providePaymentSourcesCard()
        configureGetPaymentSources(card)

        val result = sut.refreshSelectedPaymentSource()

        result.shouldBeRightAndEqualTo(card)
        assertEquals(card, sut.selectedPaymentSource.value)
    }

    @Test
    fun `when getPaymentSourceList then correct returned`() = dispatcher.runBlockingTest {
        val card = TestDataProvider.providePaymentSourcesCard()
        configureGetPaymentSources(card)

        val result = sut.getPaymentSourceList()

        result.shouldBeRightAndEqualTo(listOf(card))
    }

    @Test
    fun `when addPaymentSource then selected payment source returned`() = dispatcher.runBlockingTest {
        val newCard = TestDataProvider.provideNewCard()
        val card = TestDataProvider.providePaymentSourcesCard()
        configureAddPaymentSource(newCard, card)
        configureGetPaymentSources(card)

        val result = sut.addPaymentSource(newCard)

        result.shouldBeRightAndEqualTo(card)
        verify(aptoPlatform).addPaymentSource(eq(newCard), any())
        verify(aptoPlatform).getPaymentSources(any(), anyOrNull(), anyOrNull(), anyOrNull())
        assertEquals(card, sut.selectedPaymentSource.value)
    }

    private fun configureAddPaymentSource(
        newCard: NewCard,
        card: Card
    ) {
        whenever(aptoPlatform.addPaymentSource(eq(newCard), any())).thenAnswer {
            (it.arguments[1] as (Either<Failure, PaymentSource>) -> Unit).invoke(card.right())
        }
    }

    private fun configureGetPaymentSources(card: Card) {
        whenever(aptoPlatform.getPaymentSources(any(), anyOrNull(), anyOrNull(), anyOrNull())).thenAnswer {
            (it.arguments[0] as (Either<Failure, List<PaymentSource>>) -> Unit).invoke(listOf(card).right())
        }
    }

    private fun configureSharedPref() {
        whenever(sharedPreferences.edit()).thenReturn(editor)
        whenever(editor.putBoolean(any(), any())).thenReturn(editor)
    }
}
