package com.aptopayments.sdk.features.auth.email

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.data.user.VerificationStatus
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.auth.inputemail.InputEmailViewModel
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.Spy
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InputEmailViewModelTest : UnitTest() {

    private lateinit var inputEmailViewModel: InputEmailViewModel
    @Spy
    private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()

    @Mock
    private lateinit var verificationLiveData: MutableLiveData<Verification>

    @Rule
    @JvmField var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        inputEmailViewModel = InputEmailViewModel(analyticsManager)
    }

    @Test
    fun `test for verification with non-pending status`() {
        val verification = Verification("", "email", VerificationStatus.FAILED).apply {
            this.verificationId = "verificationId"
        }
        inputEmailViewModel.handleVerification(verification)
        verify(verificationLiveData, never()).postValue(verification)
    }

    @Test
    fun `test track is called on view loaded`() {
        inputEmailViewModel.viewLoaded()
        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.AuthInputEmail)
    }
}
