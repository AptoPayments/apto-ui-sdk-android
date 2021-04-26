package com.aptopayments.sdk.features.auth.inputphone

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.PhoneNumber
import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.mobile.data.user.VerificationStatus
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.test.*

private const val COUNTRY_CODE = "US"
private const val COUNTRY_CODE_PREFIX = "1"
private const val PHONE_NUMBER = "666777888"

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
class InputPhoneViewModelTest {
    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private val analyticsManager: AnalyticsManager = mock()
    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val aptoUiSdkProtocol: AptoUiSdkProtocol = mock()
    private val verification: Verification = mock()

    private lateinit var sut: InputPhoneViewModel

    @Before
    fun setUp() {
        sut = InputPhoneViewModel(analyticsManager, aptoPlatform, aptoUiSdkProtocol)
    }

    @Test
    fun `test for successful phone verification request`() {
        // Given
        val phoneNumber = PhoneNumber(COUNTRY_CODE_PREFIX, PHONE_NUMBER)
        val captor = argumentCaptor<PhoneNumber>()
        whenever(verification.status).thenReturn(VerificationStatus.PENDING)
        whenever(
            aptoPlatform.startPhoneVerification(captor.capture(), TestDataProvider.anyObject())
        ).thenAnswer { invocation ->
            (invocation.arguments[1] as (Either<Failure, Verification>) -> Unit).invoke(Either.Right(verification))
        }

        // When
        sut.onCountryChanged(COUNTRY_CODE)
        sut.onPhoneChanged(PHONE_NUMBER, true)
        sut.onContinueClicked()

        // Then
        assertEquals(captor.firstValue.phoneNumber, PHONE_NUMBER)
        assertEquals(captor.firstValue.countryCode, COUNTRY_CODE_PREFIX)
        verify(verification).verificationDataPoint = phoneNumber.toStringRepresentation()
        assertNotNull(sut.verificationData.getOrAwaitValue())
    }

    @Test
    fun `test for verification with non-pending status`() {
        whenever(verification.status).thenReturn(VerificationStatus.FAILED)
        whenever(
            aptoPlatform.startPhoneVerification(TestDataProvider.anyObject(), TestDataProvider.anyObject())
        ).thenAnswer { invocation ->
            (invocation.arguments[1] as (Either<Failure, Verification>) -> Unit).invoke(Either.Right(verification))
        }

        // When
        sut.onCountryChanged(COUNTRY_CODE)
        sut.onPhoneChanged(PHONE_NUMBER, true)
        sut.onContinueClicked()

        // Then
        assertNull(sut.verificationData.getOrAwaitValue())
    }

    @Test
    fun `test track is called on init`() {
        verify(analyticsManager).track(Event.AuthInputPhone)
    }

    @Test
    fun `when valid input then button is enabled`() {
        sut.onCountryChanged(COUNTRY_CODE)
        sut.onPhoneChanged(PHONE_NUMBER, true)

        assertTrue { sut.enableNextButton.getOrAwaitValue() }
    }

    @Test
    fun `when invalid input then button is disabled`() {
        sut.onCountryChanged(COUNTRY_CODE)
        sut.onPhoneChanged(PHONE_NUMBER, false)

        assertFalse(sut.enableNextButton.getOrAwaitValue())
    }

    @Test
    fun `when no interaction then button is disabled`() {
        assertFalse(sut.enableNextButton.getOrAwaitValue())
    }

    @Test
    fun `whenever embedded then X is shown`() {
        whenever(aptoUiSdkProtocol.cardOptions).thenReturn(CardOptions(openingMode = CardOptions.OpeningMode.EMBEDDED))

        assertTrue { sut.showXOnToolbar }
    }

    @Test
    fun `whenever embedded then X is not shown`() {
        whenever(aptoUiSdkProtocol.cardOptions).thenReturn(CardOptions(openingMode = CardOptions.OpeningMode.STANDALONE))

        assertFalse(sut.showXOnToolbar)
    }
}
