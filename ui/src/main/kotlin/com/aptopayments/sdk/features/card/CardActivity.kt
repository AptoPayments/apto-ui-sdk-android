package com.aptopayments.sdk.features.card

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.extension.localized
import com.aptopayments.mobile.features.managecard.CardOptions.PCIAuthType
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.removeAnimated
import com.aptopayments.sdk.core.extension.show
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.usecase.AuthenticationCompletedUseCase
import com.aptopayments.sdk.core.usecase.ForgotPinUseCase
import com.aptopayments.sdk.core.usecase.ShouldAuthenticateOnStartUpUseCase
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.ui.views.AuthenticationView
import com.aptopayments.sdk.ui.views.AuthenticationView.AuthType.FORCED
import com.aptopayments.sdk.utils.extensions.SnackbarMessageType
import com.aptopayments.sdk.utils.extensions.showCustomSnackbar
import kotlinx.android.synthetic.main.activity_layout.*
import org.koin.android.ext.android.inject
import java.lang.ref.WeakReference

class CardActivity : BaseActivity(), AuthenticationView.Delegate {

    private val shouldAuthenticateOnStartupUseCase: ShouldAuthenticateOnStartUpUseCase by inject()
    private val authCompletedUseCase: AuthenticationCompletedUseCase by inject()
    private val forgotPinUseCase: ForgotPinUseCase by inject()
    private val analyticsManager: AnalyticsServiceContract by inject()
    private var onAuthenticatedCorrectly: (() -> Unit)? = null
    private var onAuthenticatedCancelled: (() -> Unit)? = null

    private val cardFlow: CardFlow?
        get() {
            return AptoUiSdk.cardFlow?.get()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (cardFlow == null) {
            this.finish()
        }
        attachFlow()
        if (savedInstanceState == null) {
            cardFlow?.start(animated = false)
        } else {
            cardFlow?.onRestoreInstanceState()
        }
        configureBiometricView()
        removeAuthenticateScreenWhenLoggedOut()
    }

    override fun onResume() {
        super.onResume()
        attachFlow()
        tryToShowAuthenticationScreen()
    }

    private fun tryToShowAuthenticationScreen() {
        shouldAuthenticateOnStartupUseCase().runIfRight { authenticationNeeded ->
            if (authenticationNeeded) {
                authenticate(type = FORCED, onAuthenticated = { authCompletedUseCase.invoke() })
            }
        }
    }

    override fun onPause() {
        super.onPause()
        cardFlow?.detachFromActivity()
    }

    override fun onStop() {
        hideKeyboard()
        super.onStop()
    }

    override fun onDestroy() {
        cardFlow?.detachFromActivity()
        super.onDestroy()
    }

    private fun attachFlow() {
        cardFlow?.attachTo(activity = this, fragmentContainer = R.id.fragmentContainer)
    }

    private fun configureBiometricView() {
        authentication_view.delegate = this
    }

    override fun onAuthCorrect() {
        onAuthenticatedCorrectly?.invoke()
        cleanAuth()
    }

    override fun onAuthCancelled() {
        onAuthenticatedCancelled?.invoke()
        cleanAuth()
    }

    override fun onAuthPinFailed() {
        showCustomSnackbar(
            "biometric_show_pin_error_wrong_pin".localized(),
            SnackbarMessageType.ERROR
        )
    }

    override fun onAuthForgot() {
        val title = "biometric_verify_pin_forgot_alert_title".localized()
        val message = "biometric_verify_pin_forgot_alert_message".localized()
        val confirm = "biometric_verify_pin_forgot_alert_confirm".localized()
        val cancel = "biometric_verify_pin_forgot_alert_cancel".localized()

        confirm(
            title, message, confirm, cancel,
            {
                forgotPinUseCase()
                cleanAuth()
            },
            {}
        )
    }

    private fun cleanAuth() {
        authentication_view.removeAnimated()
        onAuthenticatedCorrectly = null
        onAuthenticatedCancelled = null
    }

    internal fun authenticate(
        type: AuthenticationView.AuthType,
        onlyPin: Boolean = false,
        onCancelled: (() -> Unit)? = null,
        onAuthenticated: (() -> Unit)? = null
    ) {
        analyticsManager.track(Event.VerifyPasscodeStart)
        authentication_view.show()
        this.onAuthenticatedCorrectly = onAuthenticated
        this.onAuthenticatedCancelled = onCancelled
        authentication_view.startAuthentication(this, type, getAuthType(onlyPin))
    }

    private fun getAuthType(onlyPin: Boolean) =
        if (onlyPin) {
            AuthenticationView.AuthMethod.ONLY_PIN
        } else if (AptoUiSdk.cardOptions.authenticatePCI() == PCIAuthType.PIN_OR_BIOMETRICS || AptoUiSdk.cardOptions.authenticateOnStartup()) {
            AuthenticationView.AuthMethod.BOTH
        } else {
            AuthenticationView.AuthMethod.ONLY_BIOMETRICS
        }

    private fun removeAuthenticateScreenWhenLoggedOut() {
        val weak = WeakReference(this)
        AptoPlatform.subscribeSessionInvalidListener(weak) {
            weak.get()?.cleanAuth()
            AptoPlatform.unsubscribeSessionInvalidListener(weak)
        }
    }

    companion object {
        fun callingIntent(from: Context): Intent = Intent(from, CardActivity::class.java)
    }
}
