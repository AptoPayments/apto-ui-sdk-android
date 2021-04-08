package com.aptopayments.sdk.features.biometric

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class BiometricWrapper(private val context: Context) {

    fun canAskBiometric(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS
    }

    fun showBiometricPrompt(
        activity: AppCompatActivity,
        title: String,
        subtitle: String,
        negativeButtonTitle: String,
        onAuthSuccess: (() -> Unit),
        onError: (() -> Unit)
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonTitle)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity, ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError.invoke()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthSuccess.invoke()
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}
