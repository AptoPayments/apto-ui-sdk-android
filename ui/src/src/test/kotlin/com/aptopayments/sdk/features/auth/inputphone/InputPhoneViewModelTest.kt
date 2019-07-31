package com.aptopayments.sdk.features.auth.inputphone

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.PhoneNumber
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.data.user.VerificationStatus
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.Spy
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InputPhoneViewModelTest: UnitTest() {

    private lateinit var sut: InputPhoneViewModel
    @Spy
    private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()
    @Mock
    private lateinit var verificationLiveData: MutableLiveData<Verification>
    @Rule
    @JvmField var rule: TestRule = InstantTaskExecutorRule()

    @Before
    override fun setUp() {
        super.setUp()
        sut = InputPhoneViewModel(analyticsManager)
    }

    @Test
    fun `test for successful phone verification request`() {
        runBlocking {
            // Given
            val countryCode = "US"
            val countryCodePrefix = "1"
            val number = "666777888"
            val request = PhoneNumber(countryCodePrefix, number)

            // When
            sut.startVerificationUseCase(
                    countryCode = countryCode,
                    phoneNumberInput = number).join()

            // Then
            assertEquals(sut.phoneNumber, request)
        }
    }

    @Test
    fun `test for verification with non-pending status`() {
        val verification = Verification("", "", VerificationStatus.FAILED).apply {
            this.verificationId = "verificationId"
        }
        sut.handleVerification(verification)
        verify(verificationLiveData, never()).postValue(verification)
    }

    @Test
    fun `test track is called on view loaded`() {
        sut.viewLoaded()
        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.AuthInputPhone)
    }
}
