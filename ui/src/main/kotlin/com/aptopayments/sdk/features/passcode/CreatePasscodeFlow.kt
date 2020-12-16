package com.aptopayments.sdk.features.passcode

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.core.usecase.SavePasscodeUseCase
import org.koin.core.inject

private const val PASSCODE_PIN_TAG = "PasscodeFragment"

internal class CreatePasscodeFlow(
    private val mode: PasscodeMode,
    val onFinish: () -> Unit,
    val onBack: (() -> Unit)? = null
) : Flow(), PasscodeContract.Delegate {

    private val savePasscodeUseCase: SavePasscodeUseCase by inject()

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = getCorrectFragment()

        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(PASSCODE_PIN_TAG) as? PasscodeContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onPasscodeSetCorrectly(passCode: String) {
        savePasscodeUseCase(passCode)
        onFinish()
    }

    override fun onBackPressed() {
        onBack?.invoke()
    }

    private fun getCorrectFragment(): PasscodeContract.View {
        return if (mode == PasscodeMode.CREATE) {
            fragmentFactory.createPasscodeFragment(PASSCODE_PIN_TAG)
        } else {
            fragmentFactory.changePasscodeFragment(PASSCODE_PIN_TAG)
        }
    }
}
