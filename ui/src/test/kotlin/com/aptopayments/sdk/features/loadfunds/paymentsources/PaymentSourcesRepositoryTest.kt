package com.aptopayments.sdk.features.loadfunds.paymentsources

import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.data.paymentsources.Card
import com.aptopayments.mobile.data.paymentsources.NewCard
import com.aptopayments.mobile.data.paymentsources.PaymentSource
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.aptopayments.sdk.utils.shouldBeRightAndEqualTo
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val ACCEPTED_ONBOARDING = "ADD_CARD_ACCEPTED_ONBOARDING"

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
class PaymentSourcesRepositoryTest {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

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
    fun `when repo without interaction then selectedPaymentSource is null`() {
        assertNull(sut.selectedPaymentSource.getOrAwaitValue())
    }

    @Test
    fun `when selectPaymentSourceLocally then value is updated`() = runBlockingTest {
        val card = TestDataProvider.providePaymentSourcesCard()

        sut.selectPaymentSourceLocally(card)

        assertEquals(card, sut.selectedPaymentSource.getOrAwaitValue())
    }

    @Test
    fun `when selectPaymentSourceLocally then no api-call is made`() = runBlockingTest {
        val card = TestDataProvider.providePaymentSourcesCard()

        sut.selectPaymentSourceLocally(card)

        verifyZeroInteractions(aptoPlatform)
    }

    @Test
    fun `when refreshSelectedPaymentSource then correct payment source returned in liveData`() = runBlockingTest {
        val card = TestDataProvider.providePaymentSourcesCard()
        configureGetPaymentSources(card)

        val result = sut.refreshSelectedPaymentSource()

        result.shouldBeRightAndEqualTo(card)
        assertEquals(card, sut.selectedPaymentSource.getOrAwaitValue())
    }

    @Test
    fun `when getPaymentSourceList then correct returned`() = runBlockingTest {
        val card = TestDataProvider.providePaymentSourcesCard()
        configureGetPaymentSources(card)

        val result = sut.getPaymentSourceList()

        result.shouldBeRightAndEqualTo(listOf(card))
    }

    @Test
    fun `when addPaymentSource then selected payment source returned`() = runBlockingTest {
        val newCard = TestDataProvider.provideNewCard()
        val card = TestDataProvider.providePaymentSourcesCard()
        configureAddPaymentSource(newCard, card)
        configureGetPaymentSources(card)

        val result = sut.addPaymentSource(newCard)

        result.shouldBeRightAndEqualTo(card)
        verify(aptoPlatform).addPaymentSource(eq(newCard), any())
        verify(aptoPlatform).getPaymentSources(any(), anyOrNull(), anyOrNull(), anyOrNull())
        assertEquals(card, sut.selectedPaymentSource.getOrAwaitValue())
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
