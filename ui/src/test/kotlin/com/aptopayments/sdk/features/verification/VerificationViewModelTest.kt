package com.aptopayments.sdk.features.verification

import androidx.lifecycle.MutableLiveData
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.user.DataPoint
import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.mobile.data.user.VerificationStatus
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.auth.verification.VerificationViewModel
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
class VerificationViewModelTest : UnitTest() {
    private lateinit var verificationViewModel: VerificationViewModel

    private var analyticsManager: AnalyticsServiceContract = mock()

    private val verificationLiveData: MutableLiveData<Verification> = mock()

    @BeforeEach
    fun `set up for testing`() {
        verificationViewModel = VerificationViewModel(analyticsManager)
    }

    @Test
    fun `test for verification with non-pending status`() {
        val verification = Verification("verificationId", "email", VerificationStatus.FAILED)

        verificationViewModel.finishVerification(verification.verificationId) { result -> result.isLeft }
        verify(verificationLiveData, never()).postValue(verification)
    }

    @Test
    fun `test track is called on view loaded`() {
        verificationViewModel.viewLoaded(DataPoint.Type.EMAIL)
        verify(analyticsManager).track(Event.AuthVerifyEmail)
    }
}
