package com.aptopayments.sdk.features.loadfunds.add

import com.aptopayments.mobile.data.card.*
import com.aptopayments.mobile.data.fundingsources.Balance
import com.aptopayments.mobile.data.payment.Payment
import com.aptopayments.mobile.data.payment.PaymentStatus

import com.aptopayments.mobile.data.paymentsources.PaymentSource
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.left
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.CoroutineDispatcherTest
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.applicationModule
import com.aptopayments.sdk.core.di.viewmodel.viewModelModule
import com.aptopayments.sdk.features.loadfunds.add.AddFundsViewModel.Actions
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElement
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourcesRepository
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.test.inject
import org.mockito.ArgumentMatchers
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val CARD_ID = "crd_1234"
private const val BALANCE_ID = "bal_1234"
private const val AMOUNT_LIMIT = 100.0
private const val AMOUNT_VALID = AMOUNT_LIMIT - 1
private const val PAYMENT_SOURCE_ID = "pa_so_id_1"
private const val CURRENCY = "USD"

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class)
internal class AddFundsViewModelTest : UnitTest(), CoroutineDispatcherTest {

    override lateinit var dispatcher: TestCoroutineDispatcher

    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val repo = PaymentSourcesRepository(aptoPlatform, mock())

    private val sut by inject<AddFundsViewModel> { parametersOf(CARD_ID) }

    @BeforeEach
    fun setUp() {
        configureCard()
        configureFundingSource()
        startKoin {
            modules(
                viewModelModule, applicationModule,
                module {
                    single(override = true) { aptoPlatform }
                    factory(override = true) { repo }
                }
            )
        }
    }

    @Nested
    inner class InitializationTests {
        @Test
        fun `when create class then SelectedPaymentSource is fetched`() = runBlockingTest {
            configureGetPaymentSources(TestDataProvider.providePaymentSourcesCard())

            sut.state.getOrAwaitValue()

            verify(aptoPlatform).getPaymentSources(any(), anyOrNull(), anyOrNull(), anyOrNull())
        }

        @Test
        fun `given no payment source when create class then SelectedPaymentSource is fetched`() = runBlockingTest {
            configureGetPaymentSources(null)

            val action = sut.action.getOrAwaitValue()

            assertEquals(Actions.AddPaymentSource, action)
        }

        @Test
        fun `when getState then dollar is currencySymbol`() = runBlockingTest {
            configureGetPaymentSources(null)

            val state = sut.state.getOrAwaitValue()

            assertEquals("$", state.currencySymbol)
        }

        @Test
        fun `given no input when created then amount is empty`() = runBlockingTest {
            val amount = sut.amount.getOrAwaitValue()

            assertEquals("", amount)
        }

        @Test
        fun `given no input when created then continue is disabled`() = runBlockingTest {
            val state = sut.state.getOrAwaitValue()

            assertFalse(state.continueEnabled)
        }

        @Test
        fun `given paymentSource when getState then State is correct`() = runBlockingTest {
            val source = TestDataProvider.providePaymentSourcesCard()
            configureGetPaymentSources(source)

            val state = sut.state.getOrAwaitValue()

            assertEquals("load_funds_add_money_change_card", state.paymentSourceCTAKey)
            assertEquals(source.id, state.paymentSource!!.id)
            assertEquals("", state.amountError)
            assertFalse(state.continueEnabled)
        }

        @Test
        fun `given no paymentSource when getState then State is correct`() = runBlockingTest {
            configureGetPaymentSources(null)

            val state = sut.state.getOrAwaitValue()

            assertEquals("load_funds_add_money_add_card", state.paymentSourceCTAKey)
            assertEquals(PaymentSourceElement.unsetElement(), state.paymentSource)

            assertEquals("", state.amountError)
            assertFalse(state.continueEnabled)
        }
    }

    @Nested
    inner class GivenAPaymentSourIsSet {
        @BeforeEach
        fun setUp() {
            configureGetPaymentSources(TestDataProvider.providePaymentSourcesCard())
        }

        @Test
        internal fun `when set amount less than the limit continue is Enabled`() {
            sut.amount.value = "99"

            val state = sut.state.getOrAwaitValue()

            assertTrue(state.continueEnabled)
        }

        @Test
        fun `when set amount equal to the limit continue is Disabled`() {
            sut.amount.value = "100"

            val state = sut.state.getOrAwaitValue()

            assertFalse(state.continueEnabled)
        }

        @Test
        fun `when set amount higher than the limit continue is Disabled`() {
            sut.amount.value = "101"

            val state = sut.state.getOrAwaitValue()

            assertFalse(state.continueEnabled)
        }

        @Test
        fun `when onPaymentSourceClicked then Action PaymentSourcesList is fired`() {
            sut.onPaymentSourceClicked()

            val action = sut.action.getOrAwaitValue()

            assertEquals(Actions.PaymentSourcesList, action)
        }
    }

    @Nested
    inner class GivenAPaymentSourIsSetAndCorrectAmount {
        @BeforeEach
        fun setUp() {
            configureGetPaymentSources(TestDataProvider.providePaymentSourcesCard(id = PAYMENT_SOURCE_ID))
            sut.amount.value = AMOUNT_VALID.toString()
        }

        @Test
        fun `when onContinueClicked then pushFunds is called`() {
            sut.onContinueClicked()

            verify(aptoPlatform).pushFunds(
                eq(BALANCE_ID),
                eq(PAYMENT_SOURCE_ID),
                eq(Money(CURRENCY, AMOUNT_VALID)),
                any()
            )
        }

        @Test
        fun `when onContinueClicked then event is fired`() {
            val payment = TestDataProvider.providePaymentSourcesPayment()
            configurePushFunds(payment)
            sut.onContinueClicked()

            val action = sut.action.getOrAwaitValue()

            assertEquals(payment, (action as? Actions.PaymentResult)?.payment)
        }

        @Test
        fun `given a Payment that fails when onContinueClicked then event is fired`() {
            val payment = TestDataProvider.providePaymentSourcesPayment(status = PaymentStatus.FAILED)
            configurePushFunds(payment)
            sut.onContinueClicked()

            val action = sut.failure.getOrAwaitValue()

            assertTrue(action is AddFundsViewModel.UnableToLoadFundsError)
        }

        private fun configurePushFunds(payment: Payment?) {
            whenever(aptoPlatform.pushFunds(any(), anyOrNull(), anyOrNull(), anyOrNull())).thenAnswer {
                (it.arguments[3] as (Either<Failure, Payment>) -> Unit)
                    .invoke(payment?.let { payment.right() } ?: Failure.ServerError(0).left())
            }
        }
    }

    @Test
    fun `given no paymentSource set when onPaymentSourceClicked then Action PaymentSourcesList is fired`() {
        configureGetPaymentSources(null)

        sut.onPaymentSourceClicked()

        val action = sut.action.getOrAwaitValue()
        assertEquals(Actions.AddPaymentSource, action)
    }

    private fun configureGetPaymentSources(card: PaymentSource?) {
        whenever(aptoPlatform.getPaymentSources(any(), anyOrNull(), anyOrNull(), anyOrNull())).thenAnswer {
            (it.arguments[0] as (Either<Failure, List<PaymentSource>>) -> Unit)
                .invoke(card?.let { listOf(card).right() } ?: Either.Right(emptyList()))
        }
    }

    private fun configureCard() {
        val fundingFeature =
            FundingFeature(true, emptyList(), FundingLimits(FundingSingleLimit(Money("USD", AMOUNT_LIMIT))), "")
        val card = TestDataProvider.provideCard(features = Features(funding = fundingFeature))

        whenever(
            aptoPlatform.fetchCard(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyBoolean(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(card.right())
        }
    }

    private fun configureFundingSource() {
        whenever(
            aptoPlatform.fetchCardFundingSource(
                eq(CARD_ID),
                any(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Balance>) -> Unit).invoke(Balance(id = BALANCE_ID).right())
        }
    }
}
