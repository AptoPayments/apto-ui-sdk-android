package com.aptopayments.sdk.features.auth.email

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.mobile.data.user.VerificationStatus
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.auth.inputemail.InputEmailViewModel
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class InputEmailViewModelTest {

    private lateinit var sut: InputEmailViewModel

    private var analyticsManager: AnalyticsServiceContract = mock()
    private val verificationLiveData: MutableLiveData<Verification> = mock()

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        sut = InputEmailViewModel(analyticsManager)
    }

    @Test
    fun `test for verification with non-pending status`() {
        val verification = Verification("verificationId", "email", VerificationStatus.FAILED)
        sut.handleVerification(verification)
        verify(verificationLiveData, never()).postValue(verification)
    }

    @Test
    fun `test track is called on view loaded`() {
        verify(analyticsManager).track(Event.AuthInputEmail)
    }
}
