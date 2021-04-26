package com.aptopayments.sdk.features.card.orderphysical.initial

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.Money
import com.aptopayments.mobile.data.card.OrderPhysicalCardConfig
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.left
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.card.orderphysical.initial.OrderPhysicalCardViewModel.Action
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val CARD_ID = "id_1234"

@Suppress("UNCHECKED_CAST")
internal class OrderPhysicalCardViewModelTest {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val analyticsManager: AnalyticsServiceContract = mock()

    private lateinit var sut: OrderPhysicalCardViewModel

    private fun createSut() {
        sut = OrderPhysicalCardViewModel(CARD_ID, aptoPlatform, analyticsManager)
    }

    @Test
    fun `when ViewModel created then OrderPhysicalCardStart is tracked`() {
        createSut()

        verify(analyticsManager).track(Event.OrderPhysicalCardStart)
    }

    @Test
    fun `given a positive amount when initialized then fee is displayed correctly`() {
        val money = TestDataProvider.provideMoney(10.0)
        setConfigurationCorrectWithAmount(money)

        createSut()
        val state = sut.state.getOrAwaitValue()

        assertEquals(money.toString(), state.fee)
        assertTrue(state.visibleFee)
    }

    @Test
    fun `given a zero amount when initialized then fee is hidden`() {
        setConfigurationCorrectWithAmount(TestDataProvider.provideMoney(0.0))

        createSut()
        val state = sut.state.getOrAwaitValue()

        assertEquals("", state.fee)
        assertFalse(state.visibleFee)
    }

    @Test
    fun `given an error getting the config when initialized then back action is fired`() {
        configureGetOrderPhysicalCardConfig(Failure.ServerError(0).left())

        createSut()
        val action = sut.action.getOrAwaitValue()

        assertEquals(Action.NavigateToPreviousScreen, action)
    }

    @Test
    fun `when order action is called then api gets called `() {
        setConfigurationCorrectWithAmount(TestDataProvider.provideMoney(0.0))
        createSut()

        sut.orderCard()

        verify(aptoPlatform).orderPhysicalCard(eq(CARD_ID), TestDataProvider.anyObject())
    }

    @Test
    fun `given card can be ordered when order action is called then ShowSuccessScreen action is fired`() {
        setConfigurationCorrectWithAmount(TestDataProvider.provideMoney(0.0))
        configureGetOrderPhysicalCard(TestDataProvider.provideCard(CARD_ID).right())
        configureFetchCard()
        createSut()

        sut.orderCard()

        val action = sut.action.getOrAwaitValue()
        assertEquals(Action.ShowSuccessScreen, action)
    }

    @Test
    fun `given card can be ordered when order action is called then card is refreshed`() {
        setConfigurationCorrectWithAmount(TestDataProvider.provideMoney(0.0))
        configureGetOrderPhysicalCard(TestDataProvider.provideCard(CARD_ID).right())
        configureFetchCard()
        createSut()

        sut.orderCard()

        verify(aptoPlatform).fetchCard(eq(CARD_ID), eq(true), any())
    }

    @Test
    fun `given card can't be ordered when order action is called then failure is fired`() {
        setConfigurationCorrectWithAmount(TestDataProvider.provideMoney(0.0))
        val error = Failure.ServerError(0)
        configureGetOrderPhysicalCard(error.left())
        createSut()

        sut.orderCard()

        val failure = sut.failure.getOrAwaitValue()
        assertEquals(error, failure)
    }

    @Test
    fun `when navigateBack then NavigateToPreviousScreen action is fired`() {
        createSut()

        sut.navigateBack()
        val action = sut.action.getOrAwaitValue()

        verify(analyticsManager).track(Event.OrderPhysicalCardDiscarded)
        assertEquals(Action.NavigateToPreviousScreen, action)
    }

    private fun configureGetOrderPhysicalCardConfig(result: Either<Failure, OrderPhysicalCardConfig>) {
        whenever(
            aptoPlatform.getOrderPhysicalCardConfig(eq(CARD_ID), TestDataProvider.anyObject())
        ).thenAnswer { invocation ->
            (invocation.arguments[1] as (Either<Failure, OrderPhysicalCardConfig>) -> Unit).invoke(result)
        }
    }

    private fun configureFetchCard() {
        whenever(
            aptoPlatform.fetchCard(eq(CARD_ID), any(), TestDataProvider.anyObject())
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(
                TestDataProvider.provideCard(accountID = CARD_ID).right()
            )
        }
    }

    private fun configureGetOrderPhysicalCard(answer: Either<Failure, Card>) {
        whenever(
            aptoPlatform.orderPhysicalCard(eq(CARD_ID), TestDataProvider.anyObject())
        ).thenAnswer { invocation ->
            (invocation.arguments[1] as (Either<Failure, Card>) -> Unit).invoke(answer)
        }
    }

    private fun setConfigurationCorrectWithAmount(money: Money) {
        val orderPhysicalCardConfig = OrderPhysicalCardConfig(money)
        configureGetOrderPhysicalCardConfig(orderPhysicalCardConfig.right())
    }
}
