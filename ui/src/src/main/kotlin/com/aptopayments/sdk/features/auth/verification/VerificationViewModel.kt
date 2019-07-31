package com.aptopayments.sdk.features.auth.verification

import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.data.user.DataPoint
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.data.user.VerificationStatus
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.utils.CountDown
import javax.inject.Inject

const val COUNTDOWN_TIME = 45

internal class VerificationViewModel @Inject constructor(
        private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel()
{
    var verification: MutableLiveData<Verification> = MutableLiveData()
    var pinEntryState: MutableLiveData<PINEntryState> = MutableLiveData()
    var resendButtonState: MutableLiveData<ResendButtonState> = MutableLiveData()
    private var countDown = CountDown()

    init {
        showPinEntryEnabled()
        startCountDown()
    }

    fun restartVerification(onComplete: (Either<Failure, Unit>) -> Unit){
        verification.value?.let { verification ->
            AptoPlatform.restartVerification(verification) { restartResult ->
                restartResult.either(::handleFailure) { verification ->
                    this.verification.postValue(verification)
                    startCountDown()
                    showPinEntryEnabled()
                    onComplete(Either.Right(Unit))
                }
            }
        }
    }

    fun finishVerification(secret: String, onComplete: (Either<Failure, Verification>) -> Unit) {
        verification.value?.let { verification ->
            verification.secret = secret
            AptoPlatform.completeVerification(verification) {
                it.either(::handleFailure) { result -> handleVerification(result, onComplete) }
            }
        }
    }

    private fun handleVerification(verification: Verification, onComplete: (Either<Failure, Verification>) -> Unit) {
        this.verification.postValue(verification)
        when (verification.status) {
            VerificationStatus.FAILED -> { showExpiredPin() }
            VerificationStatus.PASSED -> { stopCountDown() }
            VerificationStatus.PENDING -> {}
        }
        onComplete(Either.Right(verification))
    }

    private fun startCountDown() {
        resendButtonState.postValue(ResendButtonState.Waiting(pendingSeconds = COUNTDOWN_TIME))
        countDown.start(
                seconds = COUNTDOWN_TIME,
                fireBlock = { pendingSeconds ->
                    showCountdownState(pendingSeconds)
                },
                endBlock = {
                    showResendEnabledState()
                }
        )
    }

    private fun stopCountDown() {
        countDown.stop()
    }

    private fun showCountdownState(pendingSeconds: Int) {
        resendButtonState.postValue(ResendButtonState.Waiting(pendingSeconds))
    }

    private fun showResendEnabledState() {
        resendButtonState.postValue(ResendButtonState.Enabled)
    }

    private fun showPinEntryEnabled() {
        pinEntryState.postValue(PINEntryState.Enabled)
    }

    private fun showExpiredPin() {
        pinEntryState.postValue(PINEntryState.Expired)
    }

    fun viewLoaded(dataPointType: DataPoint.Type) {
        if (dataPointType == DataPoint.Type.EMAIL) {
            analyticsManager.track(Event.AuthVerifyEmail)
        } else {
            analyticsManager.track(Event.AuthVerifyPhone)
        }
    }
}
