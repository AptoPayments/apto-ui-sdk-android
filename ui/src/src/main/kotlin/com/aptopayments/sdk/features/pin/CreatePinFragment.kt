package com.aptopayments.sdk.features.pin

import android.view.animation.AnimationUtils
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.ui.views.SecretPinView
import kotlinx.android.synthetic.main.fragment_set_login_pin.*

internal class CreatePinFragment : BaseFragment(), CreatePinContract.View, SecretPinView.Delegate {

    override var delegate: CreatePinContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_set_login_pin

    override fun setupViewModel() {
        //TODO Track
    }

    var currentState: State = CreateState()
        set(value) {
            field = value
            value.init()
            pin_view.clean()
        }

    override fun setupUI() {
        currentState = CreateState()
        pin_view.delegate = this
        with(themeManager()) {
            customizeLargeTitleLabel(pin_title)
            customizeFooterLabel(pin_terms)
        }
    }

    override fun onPinEntered(value: String) {
        currentState.onPin(value)
    }

    override fun onForgotPressed() {
        // Nothing
    }

    override fun onBiometricPressed() {
        //Nothing
    }

    override fun onBackPressed() {
        currentState.onBack()
    }

    interface State {
        fun init()
        fun onPin(value: String)
        fun onBack()
    }

    open inner class CreateState : State {
        override fun init() {
            pin_title.localizedText = "biometric_create_pin_title"
        }

        override fun onPin(value: String) {
            currentState = ConfirmState(value)
        }

        override fun onBack() {
            delegate?.onBackPressed()
        }
    }

    inner class ConfirmState(private val firstPin : String) : State {
        override fun init() {
            pin_title.localizedText = "biometric.create_pin.confirmation_title"
        }

        override fun onPin(value: String) {
            if (firstPin == value) {
                delegate?.onPinSetCorrectly(value)
            } else {
                pin_view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
                pin_view.clean()
                notify(
                    "biometric_create_pin_error_title".localized(),
                    "biometric.create_pin_error_pin_not_match".localized()
                )
                currentState = CreateState()
            }
        }

        override fun onBack() {
            currentState = CreateState()
        }
    }

}
