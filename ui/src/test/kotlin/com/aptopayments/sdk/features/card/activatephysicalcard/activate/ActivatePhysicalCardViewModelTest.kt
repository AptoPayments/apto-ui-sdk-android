package com.aptopayments.sdk.features.card.activatephysicalcard.activate

import com.aptopayments.mobile.data.card.ActivatePhysicalCardResult
import com.aptopayments.mobile.data.card.ActivatePhysicalCardResultType
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.left
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.sdk.features.card.activatephysicalcard.activate.ActivatePhysicalCardViewModel.Action
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.*

private const val CARD_ID = "id_1234"
private const val CODE = "123456"

@Suppress("UNCHECKED_CAST")
@ExtendWith(InstantExecutorExtension::class)
internal class ActivatePhysicalCardViewModelTest {

    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val analyticsManager: AnalyticsServiceContract = mock()

    private lateinit var sut: ActivatePhysicalCardViewModel

    @BeforeEach
    internal fun setUp() {
        sut = ActivatePhysicalCardViewModel(CARD_ID, aptoPlatform, analyticsManager)
    }

    @Test
    internal fun `when sut is initialized then correct tracking is made`() {
        verify(analyticsManager).track(Event.ManageCardActivatePhysicalCard)
    }

    @Test
    internal fun `given a code when activatePhysicalCard then endpoint is called with correct code and cardId`() {

        sut.activatePhysicalCard(CODE)

        verify(aptoPlatform).activatePhysicalCard(eq(CARD_ID), eq(CODE), any())
    }

    @Test
    internal fun `given endpoint returning Failure when activatePhysicalCard then failure is thrown`() {
        val error = Failure.ServerError(0)
        configureCall(error.left())

        sut.activatePhysicalCard(CODE)
        val failure = sut.failure.getOrAwaitValue()

        assertEquals(error, failure)
    }

    @Test
    internal fun `given endpoint returning ActivatePhysicalCardResult with error when activatePhysicalCard then correct error is thrown as Action`() {
        val errorCode = "1000"
        val error = ActivatePhysicalCardResult(result = ActivatePhysicalCardResultType.ERROR, errorCode = errorCode)
        configureCall(error.right())

        sut.activatePhysicalCard(CODE)
        val action = sut.action.getOrAwaitValue()

        assertTrue(action is Action.Error)
        assertEquals(errorCode.toInt(), (action as Action.Error).failure.code)
    }

    @Test
    internal fun `given endpoint returning Activated when activatePhysicalCard then Action-Activated is fired`() {
        val result = ActivatePhysicalCardResult(result = ActivatePhysicalCardResultType.ACTIVATED)
        configureCall(result.right())

        sut.activatePhysicalCard(CODE)
        val action = sut.action.getOrAwaitValue()

        assertTrue(action is Action.Activated)
    }

    private fun configureCall(output: Either<Failure, ActivatePhysicalCardResult>) {
        whenever(
            aptoPlatform.activatePhysicalCard(
                eq(CARD_ID),
                any(),
                any()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, ActivatePhysicalCardResult>) -> Unit).invoke(
                output
            )
        }
    }
}
