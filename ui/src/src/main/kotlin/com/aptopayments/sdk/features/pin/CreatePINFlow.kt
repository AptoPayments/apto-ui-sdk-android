package com.aptopayments.sdk.features.pin

import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.core.usecase.SavePinUseCase
import org.koin.core.inject

private const val CREATE_PIN_TAG = "CreateLoginPinFragment"

internal class CreatePINFlow(
    var onFinish: () -> Unit,
    var onBack: (() -> Unit)? = null
) : Flow(), CreatePinContract.Delegate {

    private val savePinUseCase: SavePinUseCase by inject()

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = fragmentFactory.createPinFragment(
            uiTheme = UIConfig.uiTheme,
            tag = CREATE_PIN_TAG
        )
        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(CREATE_PIN_TAG) as? CreatePinContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onPinSetCorrectly(pin: String) {
        savePinUseCase(pin)
        onFinish()
    }

    override fun onBackPressed() {
        onBack?.invoke()
    }
}
