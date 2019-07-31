package com.aptopayments.sdk.utils

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.Context.FINGERPRINT_SERVICE
import android.content.Context.KEYGUARD_SERVICE
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

private const val KEY_NAME = "shiftKey"

class BiometricAuthenticator {

    private var cancellationSignal: CancellationSignal? = null

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun startAuth(context: Context, onComplete: (BiometricAuthenticationResult) -> Unit) {
        val availability = authAvailable(context)
        if (availability != BiometricAvailability.AVAILABLE) {
            onComplete(BiometricAuthenticationResult.NotAvailable(availability))
        }
        else {
            initCipher()?.let { cipher ->
                (context.getSystemService(FINGERPRINT_SERVICE) as? FingerprintManager)?.let { fingerprintManager ->
                    cancellationSignal = CancellationSignal()
                    val cryptoObject = FingerprintManager.CryptoObject(cipher)
                    fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, AuthenticationCallbackHandler(onComplete), null)
                }
            }
        }
    }

    fun stopAuth() {
        cancellationSignal?.cancel()
        cancellationSignal = null
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun initCipher(): Cipher? {
        if (!generateCipherKey()) return null
        val cipher: Cipher?
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
            KeyStore.getInstance("AndroidKeyStore")?.let { keyStore ->
                keyStore.load(null)
                (keyStore.getKey(KEY_NAME, null) as SecretKey).let { secretKey ->
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                    return cipher
                }
            }
        } catch (exc: Exception) {}
        return null
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun generateCipherKey(): Boolean {
        return try {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGenerator.init(
                    KeyGenParameterSpec.Builder(KEY_NAME,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            .setUserAuthenticationRequired(true)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                            .build())
            keyGenerator.generateKey()
            true
        } catch (exc: Exception) {
            false
        }
    }

    companion object {
        fun authAvailable(context: Context): BiometricAvailability {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                BiometricAvailability.NO_FINGERPRINT_SUPPORTED_IN_ANDROID_SDK
            } else {
                isFingerprintAuthPossible(context)
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        private fun isFingerprintAuthPossible(context: Context): BiometricAvailability {
            (context.getSystemService(FINGERPRINT_SERVICE) as? FingerprintManager)?.let {
                if (!it.isHardwareDetected) {
                    return BiometricAvailability.NO_FINGERPRINT_SUPPORTED_IN_DEVICE
                }
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                    return BiometricAvailability.FINGERPRINT_PERMISSION_REVOKED
                }
                if (!it.hasEnrolledFingerprints()) {
                    return BiometricAvailability.FINGERPRINT_NOT_CONFIGURED
                }
                val keyguardManager = context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                if (!keyguardManager.isKeyguardSecure) {
                    return BiometricAvailability.LOCK_SCREEN_SECURITY_DISABLED
                }
                return BiometricAvailability.AVAILABLE
            }
            return BiometricAvailability.NO_FINGERPRINT_SUPPORTED_IN_ANDROID_SDK
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    class AuthenticationCallbackHandler(
            val onComplete: (BiometricAuthenticationResult) -> Unit
    ) : FingerprintManager.AuthenticationCallback() {

        override fun onAuthenticationFailed() {
            onComplete(BiometricAuthenticationResult.Failure)
        }

        override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
            onComplete(BiometricAuthenticationResult.Success)
        }
    }
}
