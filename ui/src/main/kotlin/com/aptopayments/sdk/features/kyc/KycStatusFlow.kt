package com.aptopayments.sdk.features.kyc

import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow

private const val KYC_STATUS_TAG = "KycStatusFragment"

internal class KycStatusFlow(
    private val card: Card,
    private val onClose: () -> Unit,
    private val onKycPassed: () -> Unit
) : Flow(), KycStatusContract.Delegate {

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        // We can assume that the card has a kyc status here.
        val fragment = fragmentFactory.kycStatusFragment(
            kycStatus = card.kycStatus!!,
            cardID = card.accountID,
            tag = KYC_STATUS_TAG
        )
        fragment.delegate = this
        setStartElement(element = fragment as BaseFragment)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(KYC_STATUS_TAG) as? KycStatusContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onKycPassed() = onKycPassed.invoke()

    override fun onKycClosed() = onClose.invoke()
}
