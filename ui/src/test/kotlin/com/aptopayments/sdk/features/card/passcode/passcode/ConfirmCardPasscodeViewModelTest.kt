package com.aptopayments.sdk.features.card.passcode.passcode

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.card.setpin.CardPinViewModel
import com.aptopayments.sdk.utils.MainCoroutineRule
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val CARD_ID = "crd_1234"
private const val PREVIOUS_PIN = "1234"
private const val VERIFICATION_ID = "vf_1234"

@Suppress("UNCHECKED_CAST")
internal class ConfirmCardPasscodeViewModelTest {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val analyticsManager: AnalyticsServiceContract = mock()
    private val aptoPlatform: AptoPlatformProtocol = mock()

    private val sut =
        ConfirmCardPasscodeViewModel(CARD_ID, PREVIOUS_PIN, VERIFICATION_ID, analyticsManager, aptoPlatform)

    @Test
    fun `when created then correct tracking is made`() {
        verify(analyticsManager).track(Event.CardPasscodeConfirm)
    }

    @Test
    fun `when incorrect pin then WrongPin action`() {
        sut.setPin("9876")

        val result = sut.action.getOrAwaitValue()

        assertEquals(CardPinViewModel.Action.WrongPin, result)
    }

    @Test
    fun `when correct pin then CorrectPin action`() {
        configureAptoPlatform()

        sut.setPin(PREVIOUS_PIN)
        val result = sut.action.getOrAwaitValue()

        assertTrue(result is CardPinViewModel.Action.CorrectPin)
        assertEquals(PREVIOUS_PIN, result.pin)
    }

    @Test
    fun `when correct pin API is called`() {
        configureAptoPlatform()

        sut.setPin(PREVIOUS_PIN)

        verify(aptoPlatform).setCardPasscode(
            eq(CARD_ID),
            eq(PREVIOUS_PIN),
            eq(VERIFICATION_ID),
            TestDataProvider.anyObject()
        )
    }

    private fun configureAptoPlatform() {
        whenever(
            aptoPlatform.setCardPasscode(
                eq(CARD_ID),
                eq(PREVIOUS_PIN),
                eq(VERIFICATION_ID),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[3] as (Either<Failure, Unit>) -> Unit).invoke(Unit.right())
        }
    }
}
