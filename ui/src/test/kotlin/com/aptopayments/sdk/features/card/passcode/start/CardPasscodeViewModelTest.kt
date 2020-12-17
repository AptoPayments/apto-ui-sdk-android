package com.aptopayments.sdk.features.card.passcode.start

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.CardPasscodeFeature
import com.aptopayments.mobile.data.card.Features
import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.MainCoroutineRule
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val CARD_ID = "CARD_ID"

@Suppress("UNCHECKED_CAST")
internal class CardPasscodeViewModelTest {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val verification: Verification = mock()

    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val analyticsManager: AnalyticsServiceContract = mock()

    private lateinit var sut: CardPasscodeStartViewModel

    @Test
    fun `after initialization correct track is made`() {
        createSut()

        verify(analyticsManager).track(Event.CardPasscodeStart)
    }

    @Test
    fun `when verification is required and onContinueClicked then startPrimaryVerification is called`() {
        configureCard(isVerificationRequired = true)
        createSut()

        sut.onContinueClicked()

        verify(aptoPlatform).startPrimaryVerification(TestDataProvider.anyObject())
    }

    @Test
    fun `when verification is not required and onContinueClicked then startPrimaryVerification is not called`() {
        configureCard(isVerificationRequired = false)
        createSut()

        sut.onContinueClicked()

        verify(aptoPlatform, times(0)).startPrimaryVerification(TestDataProvider.anyObject())
    }

    @Test
    fun `when verification is not required then action is StartedWithoutVerification`() {
        configureCard(isVerificationRequired = false)
        createSut()

        sut.onContinueClicked()
        val action = sut.actions.getOrAwaitValue()

        assertTrue(action is CardPasscodeStartViewModel.Action.StartedWithoutVerification)
    }

    @Test
    fun `when startPrimaryVerification succeeds then action is VerificationStarted with correct Verification`() {
        configureCard(isVerificationRequired = true)
        whenever(
            aptoPlatform.startPrimaryVerification(TestDataProvider.anyObject())
        ).thenAnswer { invocation ->
            (invocation.arguments[0] as (Either<Failure, Verification>) -> Unit).invoke(verification.right())
        }
        createSut()

        sut.onContinueClicked()
        val action = sut.actions.getOrAwaitValue()

        assertTrue(action is CardPasscodeStartViewModel.Action.StartedWithVerification)
        assertEquals(verification, action.verification)
    }

    @Test
    fun `when onCancelClicked then action is Cancel `() {
        createSut()

        sut.onCancelClicked()
        val action = sut.actions.getOrAwaitValue()

        assertTrue(action is CardPasscodeStartViewModel.Action.Cancel)
    }

    private fun configureCard(isVerificationRequired: Boolean) {
        val features =
            Features(passcode = CardPasscodeFeature(isEnabled = true, isVerificationRequired = isVerificationRequired))
        val card = TestDataProvider.provideCard(accountID = CARD_ID, features = features)
        whenever(
            aptoPlatform.fetchCard(cardId = eq(CARD_ID), forceRefresh = eq(false), TestDataProvider.anyObject())
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(card.right())
        }
    }

    private fun createSut() {
        sut = CardPasscodeStartViewModel(CARD_ID, analyticsManager, aptoPlatform)
    }
}
