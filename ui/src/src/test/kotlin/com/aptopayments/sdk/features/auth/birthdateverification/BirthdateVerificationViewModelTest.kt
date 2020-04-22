package com.aptopayments.sdk.features.auth.birthdateverification

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.data.user.VerificationStatus
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.core.platform.AptoPlatformProtocol
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.utils.MainCoroutineRule
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val VERIFICATION_ID = "1234"

@Suppress("UNCHECKED_CAST")
class BirthdateVerificationViewModelTest : UnitTest() {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var analyticsManager: AnalyticsManager

    @Mock
    private lateinit var aptoPlatformProtocol: AptoPlatformProtocol

    @Mock
    private lateinit var primaryVerification: Verification

    @Mock
    private lateinit var secondaryVerification: Verification

    @Mock
    private lateinit var responseVerification: Verification

    private lateinit var sut: BirthdateVerificationViewModel

    @Before
    fun setUp() {
        sut = BirthdateVerificationViewModel(primaryVerification, analyticsManager, aptoPlatformProtocol)
    }

    @Test
    fun `when viewLoaded then correct tracking is done`() {
        sut.viewLoaded()

        verify(analyticsManager).track(Event.AuthVerifyBirthdate)
    }

    @Test
    fun `when created continue is disabled`() {
        assertFalse(sut.continueEnabled.getOrAwaitValue())
    }

    @Test
    fun `when date is set then continueButton is enabled`() {
        sut.setLocalDate(LocalDate.now())

        assertTrue { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `when continueClicked and verified then event is fired`() {
        configureMocks(VerificationStatus.PASSED)
        configurePlatformReturn()

        sut.setLocalDate(LocalDate.now())
        sut.onContinueClicked()

        assertEquals(sut.birthdateVerified.getOrAwaitValue().verification, responseVerification)
    }

    @Test
    fun `when continueClicked and not verified then show error`() {
        configureMocks(VerificationStatus.FAILED)
        configurePlatformReturn()

        sut.setLocalDate(LocalDate.now())
        sut.onContinueClicked()

        assertEquals(sut.verificationError.getOrAwaitValue(), true)
    }

    @Test
    fun `when continueClicked then call is made correctly`() {
        configureMocks(VerificationStatus.FAILED)
        val captor = argumentCaptor<Verification>()
        whenever(
            aptoPlatformProtocol.completeVerification(
                captor.capture(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[1] as (Either<Failure, Verification>) -> Unit).invoke(
                Either.Right(responseVerification)
            )
        }
        val date = LocalDate.now()

        sut.setLocalDate(date)
        sut.onContinueClicked()

        assertEquals(captor.firstValue.verificationId, VERIFICATION_ID)
        assertEquals(captor.firstValue.verificationType, "birthdate")
        assertEquals(captor.firstValue.secret, date.format(DateTimeFormatter.ISO_DATE))
    }

    private fun configureMocks(status: VerificationStatus) {
        whenever(secondaryVerification.verificationId).thenReturn(VERIFICATION_ID)
        whenever(primaryVerification.secondaryCredential).thenReturn(secondaryVerification)
        whenever(responseVerification.status).thenReturn(status)
    }

    private fun configurePlatformReturn() {
        whenever(
            aptoPlatformProtocol.completeVerification(
                any(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[1] as (Either<Failure, Verification>) -> Unit).invoke(
                Either.Right(responseVerification)
            )
        }
    }
}
