package com.aptopayments.sdk.ui.views

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.invisibleIf
import com.aptopayments.sdk.core.extension.visibleIf
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.core.usecase.BiometricsAuthCorrectUseCase
import com.aptopayments.sdk.core.usecase.CanAskBiometricsUseCase
import com.aptopayments.sdk.core.usecase.VerifyPasscodeUseCase
import com.aptopayments.sdk.features.biometric.BiometricWrapper
import com.aptopayments.sdk.ui.views.AuthenticationView.AuthMethod.ONLY_BIOMETRICS
import com.aptopayments.sdk.ui.views.AuthenticationView.AuthMethod.ONLY_PIN
import com.aptopayments.sdk.utils.shake
import kotlinx.android.synthetic.main.view_authentication.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject

private val DEFAULT_AUTH_TYPE = AuthenticationView.AuthType.FORCED
private val DEFAULT_AUTH_METHOD = AuthenticationView.AuthMethod.BOTH

internal class AuthenticationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), KoinComponent, SecretPinView.Delegate {

    private val canAskBiometricsUseCase: CanAskBiometricsUseCase by inject()
    private val verifyPasscodeUseCase: VerifyPasscodeUseCase by inject()
    private val biometricsAuthCorrectUseCase: BiometricsAuthCorrectUseCase by inject()
    private val biometricWrapper: BiometricWrapper by inject()

    private var isAuthenticating = false
    private var authType = DEFAULT_AUTH_TYPE
    private var authMethod = DEFAULT_AUTH_METHOD
    private lateinit var keyListener: (View, Int, KeyEvent) -> Boolean
    var delegate: Delegate? = null

    interface Delegate {
        fun onAuthCorrect()
        fun onAuthCancelled()
        fun onAuthPinFailed()
        fun onAuthForgot()
    }

    init {
        configureView(context)
        configureUi()
        createKeyListener()
        secret_pin_view.delegate = this
    }

    fun startAuthentication(activity: BaseActivity, type: AuthType, method: AuthMethod = AuthMethod.BOTH) {
        configureKeyListener()
        configureAuthType(type)
        isAuthenticating = true
        authMethod = method
        showBiometricDialogIfPossible(activity)
    }

    private fun configureAuthType(type: AuthType) {
        authType = type
        iv_close_button.visibleIf(isAuthOptional())
    }

    private fun createKeyListener() {
        keyListener = { _, keyCode: Int, event: KeyEvent ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                if (isAuthOptional())
                    authCancelled()
                true
            } else {
                false
            }
        }
    }

    private fun isAuthOptional() = authType == AuthType.OPTIONAL

    private fun authCancelled() {
        delegate?.onAuthCancelled()
        clearAuthentication()
    }

    private fun configureKeyListener() {
        requestFocus()
        setOnKeyListener(keyListener)
    }

    private fun configureView(context: Context) {
        inflate(context, R.layout.view_authentication, this)
        isClickable = true
        isFocusableInTouchMode = true
        setBackgroundColor(UIConfig.uiBackgroundSecondaryColor)
    }

    private fun configureUi() = with(themeManager()) {
        loadLogoOnImageView(client_logo)
        iv_close_button.setColorFilter(UIConfig.textTopBarPrimaryColor, PorterDuff.Mode.SRC_IN)
        iv_close_button.setOnClickListener { authCancelled() }
    }

    private fun authenticationEndedCorrectly() {
        clearAuthentication()
        delegate?.onAuthCorrect()
    }

    private fun clearAuthentication() {
        isAuthenticating = false
        setOnKeyListener(null)
        clearFocus()
        secret_pin_view.clean()
    }

    private fun showBiometricDialogIfPossible(activity: BaseActivity) {
        secret_pin_view.invisibleIf(isOnlyBiometricsMethod())
        if (!isOnlyPinMethod()) {
            canAskBiometricsUseCase().either({}, { canAsk ->
                if (canAsk) {
                    showBiometricDialog(activity)
                }
            })
        }
    }

    private fun isOnlyPinMethod() = ONLY_PIN == authMethod

    private fun isOnlyBiometricsMethod() = ONLY_BIOMETRICS == authMethod

    private fun showBiometricDialog(activity: BaseActivity) {
        biometricWrapper.showBiometricPrompt(
            activity,
            "auth_verify_biometric_title".localized(),
            "",
            getBiometricsCancelText(),
            {
                biometricsAuthCorrectUseCase()
                authenticationEndedCorrectly()
            },
            {
                if (isOnlyBiometricsMethod() && isAuthOptional()) {
                    authCancelled()
                }
            })
    }

    private fun getBiometricsCancelText() =
        if (isOnlyBiometricsMethod()) "auth_verify_biometric_cancel".localized() else "auth_verify_biometric_use_passcode".localized()

    enum class AuthType {
        FORCED, OPTIONAL
    }

    enum class AuthMethod {
        BOTH, ONLY_PIN, ONLY_BIOMETRICS
    }

    override fun onPinEntered(currentPin: String) {
        verifyPasscodeUseCase(currentPin).either({}, { isCorrect ->
            if (isCorrect) {
                authenticationEndedCorrectly()
            } else {
                pinEnteredWrong()
            }
        })
    }

    private fun pinEnteredWrong() {
        secret_pin_view.shake()
        secret_pin_view.clean()
        delegate?.onAuthPinFailed()
    }

    override fun onForgotPressed() {
        delegate?.onAuthForgot()
    }
}
