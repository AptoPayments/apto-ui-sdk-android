package com.aptopayments.sdk.features.loadfunds.result

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.Features
import com.aptopayments.mobile.data.card.FundingFeature
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.loadfunds.result.AddFundsResultViewModel.Action
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.anyBoolean
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val CARD_ID = "card_1234"
private const val CARD_PRODUCT_ID = "12345678"
private const val SOFT_DESCRIPTOR = "My company"

@Suppress("UNCHECKED_CAST")
class AddFundsResultViewModelTest {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private val fundingFeature: FundingFeature = mock {
        on { softDescriptor } doReturn SOFT_DESCRIPTOR
    }
    private val features: Features = mock {
        on { funding } doReturn fundingFeature
    }
    private val cardholderAgreement: Content = mock()
    private val card = TestDataProvider.provideCard(cardProductID = CARD_PRODUCT_ID, features = features)
    private val cardProduct =
        TestDataProvider.provideCardProduct(id = CARD_PRODUCT_ID, cardholderAgreement = cardholderAgreement)
    private val payment = TestDataProvider.providePaymentSourcesPayment()
    private val paymentResultElement: PaymentResultElement = mock()
    private val mapper: PaymentResultElementMapper = mock {
        on { map(payment, SOFT_DESCRIPTOR) } doReturn paymentResultElement
    }

    private val aptoPlatform: AptoPlatformProtocol = mock()

    private lateinit var sut: AddFundsResultViewModel

    @Test
    fun `when Done clicked then correct action is fired`() {
        createSut()

        sut.onDoneClicked()

        assertEquals(Action.Done, sut.action.getOrAwaitValue())
    }

    @Test
    fun `when Agreement clicked then correct action is fired`() {
        configureCard()
        configureCardProduct()
        createSut()

        sut.onAgreementClicked()
        val action = sut.action.getOrAwaitValue()

        assertTrue(action is Action.Agreement)
        assertEquals(cardholderAgreement, action.content)
    }

    @Test
    fun `correct results are shown`() {
        configureCard()
        configureCardProduct()
        createSut()

        val element = sut.resultElement.getOrAwaitValue()

        assertEquals(paymentResultElement, element)
    }

    private fun createSut() {
        sut = AddFundsResultViewModel(CARD_ID, payment, mapper, aptoPlatform)
    }

    private fun configureCard() {
        whenever(
            aptoPlatform.fetchCard(
                eq(CARD_ID),
                anyBoolean(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(Either.Right(card))
        }
    }

    private fun configureCardProduct() {
        whenever(
            aptoPlatform.fetchCardProduct(eq(CARD_PRODUCT_ID), anyBoolean(), TestDataProvider.anyObject())
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, CardProduct>) -> Unit).invoke(Either.Right(cardProduct))
        }
    }
}
