package com.aptopayments.sdk.features.card.passcode

import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.auth.verification.VerificationFlow
import com.aptopayments.sdk.features.card.passcode.passcode.SetPasscodeFlow
import com.aptopayments.sdk.features.card.passcode.start.CardPasscodeStartContract

private const val START_TAG = "CardPasscodeStartFragment"

internal class CardPasscodeFlow(
    private val cardId: String,
    private val onFinish: () -> Unit
) : Flow(),
    CardPasscodeStartContract.Delegate {

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = fragmentFactory.cardPasscodeStartFragment(cardId, START_TAG)
        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(START_TAG) as? CardPasscodeStartContract.View)?.let { it.delegate = this }
    }

    override fun onBackFromPasscodeStart() {
        onFinish()
    }

    override fun onStartedWithVerification(verification: Verification) {
        val flow = VerificationFlow(
            verification,
            onBack = {
                popFlow(true)
            },
            onVerified = {
                onVerificationFinished(it.verification?.verificationId)
            }
        )
        flow.init { initResult -> initResult.either(::handleFailure) { push(flow) } }
    }

    override fun onStartedWithoutVerification() {
        setPasscode()
    }

    private fun onVerificationFinished(verificationId: String?) {
        setPasscode(verificationId)
    }

    private fun setPasscode(verificationId: String? = null) {
        val flow = SetPasscodeFlow(
            cardId = cardId,
            verificationId = verificationId,
            onBack = {
                onFinish.invoke()
            },
            onFinish = {
                onFinish.invoke()
            }
        )
        flow.init { initResult -> initResult.either(::handleFailure) { push(flow) } }
    }
}
