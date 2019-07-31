package com.aptopayments.sdk.utils

import android.content.Context
import com.aptopayments.core.extension.localized

enum class BiometricAvailability {
    NO_FINGERPRINT_SUPPORTED_IN_ANDROID_SDK,
    NO_FINGERPRINT_SUPPORTED_IN_DEVICE,
    FINGERPRINT_PERMISSION_REVOKED,
    FINGERPRINT_NOT_CONFIGURED,
    LOCK_SCREEN_SECURITY_DISABLED,
    AVAILABLE;

    fun toLocalizedDescription(context: Context): String {
        return when (this) {
            NO_FINGERPRINT_SUPPORTED_IN_ANDROID_SDK -> "biometrics_no_fingerprint_supported_in_android_sdk".localized(context)
            NO_FINGERPRINT_SUPPORTED_IN_DEVICE -> "biometrics_no_fingerprint_supported_in_device".localized(context)
            FINGERPRINT_PERMISSION_REVOKED -> "biometrics_fingerprint_permission_revoked".localized(context)
            FINGERPRINT_NOT_CONFIGURED -> "biometrics_fingerprint_not_configured".localized(context)
            LOCK_SCREEN_SECURITY_DISABLED -> "biometrics_lock_screen_security_disabled".localized(context)
            AVAILABLE -> "biometrics_available".localized(context)        }
    }
}

sealed class BiometricAuthenticationResult {
    object Success : BiometricAuthenticationResult()
    object Failure : BiometricAuthenticationResult()
    class NotAvailable(reason: BiometricAvailability) : BiometricAuthenticationResult()
}
