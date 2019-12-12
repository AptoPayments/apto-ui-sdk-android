package com.aptopayments.sdk.ui.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.core.usecase.BiometricsAuthCorrectUseCase
import com.aptopayments.sdk.core.usecase.CanAskBiometricsUseCase
import com.aptopayments.sdk.core.usecase.VerifyPinUseCase
import com.aptopayments.sdk.features.biometric.BiometricWrapper
import kotlinx.android.synthetic.main.view_authentication.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.ref.WeakReference

private val DEFAULT_AUTH_TYPE = AuthenticationView.AuthType.FORCED

internal class AuthenticationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), KoinComponent, SecretPinView.Delegate {

    private val canAskBiometricsUseCase: CanAskBiometricsUseCase by inject()
    private val verifyPinUseCase: VerifyPinUseCase by inject()
    private val biometricsAuthCorrectUseCase: BiometricsAuthCorrectUseCase by inject()
    private val biometricWrapper: BiometricWrapper by inject()

    private var isAuthenticating = false
    private var authType = DEFAULT_AUTH_TYPE
    private var weakActivity: WeakReference<BaseActivity>? = null
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

    fun startAuthentication(activity: BaseActivity, type: AuthType, onlyPin: Boolean = false) {
        configureKeyListener()
        authType = type
        isAuthenticating = true
        weakActivity = WeakReference(activity)
        showBiometricDialogIfPossible(activity, onlyPin)
    }

    private fun createKeyListener() {
        keyListener = { _, keyCode: Int, event: KeyEvent ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                if (authType == AuthType.OPTIONAL) {
                    delegate?.onAuthCancelled()
                    clearAuthentication()
                }
                true
            } else {
                false
            }
        }
    }

    private fun configureKeyListener() {
        requestFocus()
        setOnKeyListener(keyListener)
    }

    private fun configureView(context: Context) {
        inflate(context, R.layout.view_authentication, this)
        isClickable = true
        isFocusableInTouchMode = true
        setBackgroundColor(Color.WHITE)
    }

    private fun configureUi() {
        themeManager().loadLogoOnImageView(client_logo)
    }

    private fun authenticationEndedCorrectly() {
        clearAuthentication()
        delegate?.onAuthCorrect()
    }

    private fun clearAuthentication() {
        authType = DEFAULT_AUTH_TYPE
        isAuthenticating = false
        setOnKeyListener(null)
        clearFocus()
        secret_pin_view.clean()
    }

    private fun showBiometricDialogIfPossible(activity: BaseActivity, onlyPin: Boolean) {
        if (!onlyPin) {
            canAskBiometricsUseCase().either({}, { canAsk ->
                if (canAsk) {
                    showBiometricDialog(activity)
                }
            })
        }
    }

    private fun showBiometricDialog(activity: BaseActivity) {
        biometricWrapper.showBiometricPrompt(
            activity,
            "auth_verify_biometric_title".localized(),
            "",
            "auth_verify_biometric_use_passcode".localized(),
            {
                biometricsAuthCorrectUseCase()
                authenticationEndedCorrectly()
            },
            {})
    }

    enum class AuthType {
        FORCED, OPTIONAL
    }

    override fun onPinEntered(currentPin: String) {
        verifyPinUseCase(currentPin).either({}, { isCorrect ->
            if (isCorrect) {
                authenticationEndedCorrectly()
            } else {
                pinEnteredWrong()
            }
        })
    }

    private fun pinEnteredWrong() {
        secret_pin_view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
        secret_pin_view.clean()
        delegate?.onAuthPinFailed()
    }

    override fun onForgotPressed() {
        delegate?.onAuthForgot()
    }

    override fun onBiometricPressed() {
        weakActivity?.get()?.let {
            showBiometricDialog(it)
        }
    }
}
