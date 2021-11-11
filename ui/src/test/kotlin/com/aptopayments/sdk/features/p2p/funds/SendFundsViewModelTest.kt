package com.aptopayments.sdk.features.p2p.funds

import androidx.lifecycle.Observer
import com.aptopayments.mobile.data.card.Money
import com.aptopayments.mobile.data.fundingsources.Balance
import com.aptopayments.mobile.data.payment.PaymentStatus
import com.aptopayments.mobile.data.transfermoney.CardHolderData
import com.aptopayments.mobile.data.transfermoney.CardHolderName
import com.aptopayments.mobile.data.transfermoney.P2pTransferResponse
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.applicationModule
import com.aptopayments.sdk.core.di.viewmodel.viewModelModule
import com.aptopayments.sdk.features.p2p.funds.SendFundsViewModel.Action
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.test.inject
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.kotlin.*

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val CARD_ID = "crd_1234"
private const val RECIPIENT_ID = "recipient_1234"
private const val BALANCE_ID = "crd_1234"
private const val BALANCE_AMOUNT = 1001.0
private const val BALANCE_CURRENCY = "USD"

@Suppress("UNCHECKED_CAST")
@ExtendWith(InstantExecutorExtension::class)
internal class SendFundsViewModelTest : UnitTest() {

    private val recipient = CardHolderData(name = CardHolderName("John", "Smith"), RECIPIENT_ID)
    private val fundingSource = TestDataProvider.provideBalance(
        id = BALANCE_ID,
        amountSpendable = Money(BALANCE_CURRENCY, BALANCE_AMOUNT)
    )
    private val observer: Observer<SendFundsViewModel.State> = mock()

    private val aptoPlatform: AptoPlatformProtocol = mock()

    private val sut by inject<SendFundsViewModel> { parametersOf(CARD_ID, recipient) }

    @BeforeEach
    fun setUp() {
        configureBalance()
        startKoin {
            modules(
                viewModelModule, applicationModule,
                module {
                    single(override = true) { aptoPlatform }
                }
            )
        }
        sut.state.observeForever(observer)
    }

    @AfterEach
    fun tearDown() {
        sut.state.removeObserver(observer)
    }

    @Test
    internal fun `given balance when viewModel created then state is correct`() {
        val money = Money(BALANCE_CURRENCY, BALANCE_AMOUNT)

        val state = sut.state.getOrAwaitValue()

        assertFalse(state.ctaEnabled)
        assertFalse(state.amountError)
        assertEquals(money.currencySymbol(), state.currencySymbol)
        assertEquals(BALANCE_AMOUNT.toString(), state.maxSpendable)
        assertEquals(recipient.name.toString(), state.recipient.title)
    }

    @Test
    internal fun `given amount less than limit and bigger than zero then continue is enabled`() {
        sut.amount.value = (BALANCE_AMOUNT - 1).toString()

        val state = sut.state.getOrAwaitValue()

        assertTrue(state.ctaEnabled)
        assertFalse(state.amountError)
    }

    @Test
    internal fun `given amount equal to the limit then continue is enabled`() {
        sut.amount.value = BALANCE_AMOUNT.toString()

        val state = sut.state.getOrAwaitValue()

        assertTrue(state.ctaEnabled)
        assertFalse(state.amountError)
    }

    @Test
    internal fun `given amount bigger than the limit then continue is not enabled and error is shown`() {
        sut.amount.value = (BALANCE_AMOUNT + 1).toString()

        val state = sut.state.getOrAwaitValue()

        assertFalse(state.ctaEnabled)
        assertTrue(state.amountError)
    }

    @Test
    internal fun `given correct amount when cta is clicked then api is called`() {
        sut.amount.value = BALANCE_AMOUNT.toString()

        sut.onCtaClicked()

        verify(aptoPlatform).p2pMakeTransfer(
            eq(BALANCE_ID),
            eq(recipient.cardholderId),
            eq(Money(fundingSource.getSpendable().currency, BALANCE_AMOUNT)),
            any()
        )
    }

    @Test
    internal fun `given correct value and Processed respones the PaymentSuccess is emitted as action`() {
        val response = TestDataProvider.provideP2pTransferResponse(status = PaymentStatus.PROCESSED)
        configureTransfer(response.right())
        sut.amount.value = BALANCE_AMOUNT.toString()

        sut.onCtaClicked()

        val action = sut.action.getOrAwaitValue()

        assertTrue(action is Action.PaymentSuccess)
        assertEquals(response, action.payment)
    }

    @Test
    internal fun `given correct value and Pending respones the PaymentSuccess is emitted as action`() {
        val response = TestDataProvider.provideP2pTransferResponse(status = PaymentStatus.PENDING)
        configureTransfer(response.right())
        sut.amount.value = BALANCE_AMOUNT.toString()

        sut.onCtaClicked()

        val action = sut.action.getOrAwaitValue()

        assertTrue(action is Action.PaymentSuccess)
        assertEquals(response, action.payment)
    }

    @Test
    internal fun `given correct value and Processed respones the PaymentFailure is emitted as action`() {
        val response = TestDataProvider.provideP2pTransferResponse(status = PaymentStatus.FAILED)
        configureTransfer(response.right())
        sut.amount.value = BALANCE_AMOUNT.toString()

        sut.onCtaClicked()

        val action = sut.action.getOrAwaitValue()

        assertTrue(action is Action.PaymentFailure)
    }

    @Test
    internal fun `when changeRecipient then ChangeRecipientAction is emitted`() {
        sut.onChangeRecipient()

        val action = sut.action.getOrAwaitValue()

        assertTrue(action is Action.ChangeRecipient)
    }

    private fun configureBalance() {
        whenever(
            aptoPlatform.fetchCardFundingSource(
                eq(CARD_ID),
                anyBoolean(),
                any()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Balance>) -> Unit).invoke(fundingSource.right())
        }
    }

    private fun configureTransfer(response: Either<Failure, P2pTransferResponse>) {
        whenever(
            aptoPlatform.p2pMakeTransfer(
                eq(BALANCE_ID),
                eq(RECIPIENT_ID),
                any(),
                any()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[3] as (Either<Failure, P2pTransferResponse>) -> Unit).invoke(response)
        }
    }
}
