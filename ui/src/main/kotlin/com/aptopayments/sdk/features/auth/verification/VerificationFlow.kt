package com.aptopayments.sdk.features.auth.verification

import com.aptopayments.mobile.data.user.DataPoint
import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import org.koin.core.KoinComponent

private const val PHONE_VERIFICATION_TAG = "PhoneVerificationFragment"
private const val EMAIL_VERIFICATION_TAG = "EmailVerificationFragment"

internal class VerificationFlow(
    val verification: Verification,
    var onBack: () -> Unit,
    var onVerified: (dataPoint: DataPoint) -> Unit
) : Flow(), PhoneVerificationContract.Delegate, EmailVerificationContract.Delegate, KoinComponent {

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = when (verification.verificationType) {
            DataPoint.Type.EMAIL.toString() -> onEmailVerificationStarted(verification) as FlowPresentable
            else -> onPhoneVerificationStarted(verification) as FlowPresentable
        }
        setStartElement(fragment)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(PHONE_VERIFICATION_TAG) as? PhoneVerificationContract.View)?.let { it.delegate = this }
        (fragmentWithTag(EMAIL_VERIFICATION_TAG) as? EmailVerificationContract.View)?.let { it.delegate = this }
    }

    override fun onBackFromPhoneVerification() = onBack.invoke()

    override fun onPhoneVerificationPassed(dataPoint: DataPoint) = onVerified.invoke(dataPoint)

    override fun onBackFromEmailVerification() = onBack.invoke()

    override fun onEmailVerificationPassed(dataPoint: DataPoint) = onVerified.invoke(dataPoint)

    private fun onPhoneVerificationStarted(verification: Verification): PhoneVerificationContract.View {
        val fragment = fragmentFactory.phoneVerificationFragment(
            verification, PHONE_VERIFICATION_TAG
        )
        fragment.delegate = this
        return fragment
    }

    private fun onEmailVerificationStarted(verification: Verification): EmailVerificationContract.View {
        val fragment = fragmentFactory.emailVerificationFragment(
            verification, EMAIL_VERIFICATION_TAG
        )
        fragment.delegate = this
        return fragment
    }
}
