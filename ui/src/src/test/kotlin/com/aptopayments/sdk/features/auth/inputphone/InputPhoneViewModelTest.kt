package com.aptopayments.sdk.features.auth.inputphone

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.PhoneNumber
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.data.user.VerificationStatus
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.core.platform.AptoPlatformProtocol
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import kotlin.test.*

private const val COUNTRY_CODE = "US"
private const val COUNTRY_CODE_PREFIX = "1"
private const val PHONE_NUMBER = "666777888"

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
class InputPhoneViewModelTest : UnitTest() {
    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var analyticsManager: AnalyticsManager

    @Mock
    private lateinit var aptoPlatform: AptoPlatformProtocol

    @Mock
    private lateinit var verification: Verification

    private lateinit var sut: InputPhoneViewModel

    @Before
    fun setUp() {
        sut = InputPhoneViewModel(analyticsManager, aptoPlatform)
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
    fun `test track is called on view loaded`() {
        sut.viewLoaded()

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
}
