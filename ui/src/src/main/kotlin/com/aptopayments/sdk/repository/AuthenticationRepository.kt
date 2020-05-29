package com.aptopayments.sdk.repository

import android.content.SharedPreferences
import com.aptopayments.core.functional.Either
import com.aptopayments.core.platform.AptoPlatformProtocol
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

private const val LAST_BIOMETRIC_AUTH = "LAST_BIOMETRIC_AUTH"
private const val BIOMETRIC_ENABLED_BY_USER = "BIOMETRIC_ENABLED_BY_USER"
private const val PIN_VALUE = "BIOMETRIC_AUTHORIZED"
private const val NEED_AUTHENTICATION = "NEED_AUTHENTICATION"
private const val DEFAULT_AUTH_TIME = 0L
private const val DEFAULT_PIN_VALUE = ""
private const val AUTHENTICATION_LIMIT_THRESHOLD: Long = 60L

internal interface AuthenticationRepository {
    fun saveAuthenticationTime()
    fun isAuthTimeInvalid(): Boolean
    fun saveNeedToAuthenticate()
    fun isAuthenticationNeedSaved(): Boolean
    fun isBiometricsEnabledByUser(): Boolean
    fun enableBiometrics(value: Boolean)
    fun isPasscodeSet(): Boolean
    fun setPasscode(value: String)
    fun getPasscode(): String
}

internal class AuthenticationRepositoryImpl(
    private val sharedPref: SharedPreferences,
    private val aptoPlatform: AptoPlatformProtocol
) : AuthenticationRepository {

    init {
        aptoPlatform.subscribeSessionInvalidListener(this) {
            sharedPref.edit().clear().apply()
        }
    }

    protected fun finalize() {
        aptoPlatform.unsubscribeSessionInvalidListener(this)
    }

    override fun saveAuthenticationTime() {
        val epoch = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        sharedPref.edit().putLong(LAST_BIOMETRIC_AUTH, epoch).apply()
    }

    override fun isAuthTimeInvalid(): Boolean {
        return getAuthenticationTime().either(
            { true },
            { it.plusSeconds(AUTHENTICATION_LIMIT_THRESHOLD).isBefore(LocalDateTime.now()) }
        )
    }

    override fun saveNeedToAuthenticate() {
        sharedPref.edit().putBoolean(NEED_AUTHENTICATION, true).commit()
    }

    override fun isAuthenticationNeedSaved() = sharedPref.getBoolean(NEED_AUTHENTICATION, false)

    override fun isBiometricsEnabledByUser() = sharedPref.getBoolean(BIOMETRIC_ENABLED_BY_USER, true)

    override fun enableBiometrics(value: Boolean) {
        sharedPref.edit().putBoolean(BIOMETRIC_ENABLED_BY_USER, value).commit()
    }

    override fun isPasscodeSet() = sharedPref.getString(PIN_VALUE, DEFAULT_PIN_VALUE) != DEFAULT_PIN_VALUE

    override fun setPasscode(value: String) {
        sharedPref.edit().putString(PIN_VALUE, value).commit()
    }

    override fun getPasscode() = sharedPref.getString(PIN_VALUE, DEFAULT_PIN_VALUE)!!

    private fun getAuthenticationTime(): Either<Unit, LocalDateTime> {
        val lastAuth = sharedPref.getLong(LAST_BIOMETRIC_AUTH, DEFAULT_AUTH_TIME)
        return if (lastAuth == DEFAULT_AUTH_TIME) {
            Either.Left(Unit)
        } else {
            Either.Right(getLastAuthenticationDateTime(lastAuth))
        }
    }

    private fun getLastAuthenticationDateTime(lastAuth: Long): LocalDateTime {
        return LocalDateTime.ofEpochSecond(lastAuth, 0, ZoneOffset.UTC)
    }
}
