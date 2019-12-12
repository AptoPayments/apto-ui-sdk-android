package com.aptopayments.sdk.features.auth

import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.AuthCredential
import com.aptopayments.core.data.config.ContextConfiguration
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.user.DataPoint
import com.aptopayments.core.data.user.DataPointList
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.data.user.VerificationStatus
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.auth.birthdateverification.BirthdateVerificationContract
import com.aptopayments.sdk.features.auth.inputemail.InputEmailContract
import com.aptopayments.sdk.features.auth.inputphone.InputPhoneContract
import com.aptopayments.sdk.features.auth.verification.EmailVerificationContract
import com.aptopayments.sdk.features.auth.verification.PhoneVerificationContract
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.reflect.Modifier

private const val INPUT_PHONE_TAG = "InputPhoneFragment"
private const val PHONE_VERIFICATION_TAG = "PhoneVerificationFragment"
private const val EMAIL_VERIFICATION_TAG = "EmailVerificationFragment"
private const val BIRTHDATE_VERIFICATION_TAG = "BirthdateVerificationFragment"
private const val INPUT_EMAIL_TAG = "InputEmailFragment"
private const val AUTH_TYPE_EMAIL = "email"
private const val AUTH_TYPE_PHONE = "phone"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class AuthFlow (
        val contextConfiguration: ContextConfiguration,
        var onBack: (Unit) -> Unit,
        var onFinish: (userToken: String) -> Unit
) : Flow(), InputPhoneContract.Delegate, InputEmailContract.Delegate, PhoneVerificationContract.Delegate,
        EmailVerificationContract.Delegate, BirthdateVerificationContract.Delegate, KoinComponent {

    val analyticsManager: AnalyticsServiceContract by inject()
    private var primaryVerification: Verification? = null

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        when (contextConfiguration.projectConfiguration.authCredential) {
            AuthCredential.PHONE -> {
                val fragment = fragmentFactory.inputPhoneFragment(
                        UIConfig.uiTheme,
                        contextConfiguration.projectConfiguration.allowedCountries,
                        INPUT_PHONE_TAG)
                fragment.delegate = this
                setStartElement(element = fragment as FlowPresentable)
            }
            AuthCredential.EMAIL -> {
                val fragment = fragmentFactory.inputEmailFragment(
                        uiTheme = UIConfig.uiTheme,
                        tag = INPUT_EMAIL_TAG
                )
                fragment.delegate = this
                setStartElement(fragment as FlowPresentable)
            }
            else -> {
                val fragment = fragmentFactory.inputPhoneFragment(
                        UIConfig.uiTheme,
                        contextConfiguration.projectConfiguration.allowedCountries,
                        INPUT_PHONE_TAG)
                fragment.delegate = this
                setStartElement(element = fragment as FlowPresentable)
            }
        }
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(INPUT_PHONE_TAG) as? InputPhoneContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(INPUT_EMAIL_TAG) as? InputEmailContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(PHONE_VERIFICATION_TAG) as? PhoneVerificationContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(BIRTHDATE_VERIFICATION_TAG) as? BirthdateVerificationContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(EMAIL_VERIFICATION_TAG) as? EmailVerificationContract.View)?.let {
            it.delegate = this
        }
    }

    //
    // Input Phone
    //
    override fun onBackFromInputPhone() = onBack(Unit)

    override fun onPhoneVerificationStarted(verification: Verification) {
        val fragment = fragmentFactory.phoneVerificationFragment(
                UIConfig.uiTheme,
                verification,
                PHONE_VERIFICATION_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    //
    // Input Email
    //
    override fun onBackFromInputEmail() = onBack(Unit)

    override fun onEmailVerificationStarted(verification: Verification) {
        val fragment = fragmentFactory.emailVerificationFragment(
                UIConfig.uiTheme,
                verification,
                EMAIL_VERIFICATION_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    //
    // Verify Phone
    //
    override fun onBackFromPhoneVerification() = popFragment()

    override fun onPhoneVerificationPassed(dataPoint: DataPoint) {
        dataPoint.verification?.let { verification ->
            verification.secondaryCredential?.let {
                popFragment()
                primaryVerification = verification
                if (it.status == VerificationStatus.PASSED) {
                    loginUser(verification, it)
                } else {
                    if (it.verificationType == AUTH_TYPE_EMAIL) {
                        val fragment = fragmentFactory.inputEmailFragment(
                                uiTheme = UIConfig.uiTheme,
                                tag = INPUT_EMAIL_TAG
                        )
                        fragment.delegate = this
                        setStartElement(fragment as FlowPresentable)
                    } else {
                        val fragment = fragmentFactory.birthdateVerificationFragment(
                                UIConfig.uiTheme,
                                dataPoint,
                                BIRTHDATE_VERIFICATION_TAG)
                        fragment.delegate = this
                        push(fragment as BaseFragment)
                    }
                }
            } ?: createUser(dataPoint)
        }
    }

    //
    // Verify Email
    //
    override fun onBackFromEmailVerification() = popFragment()

    override fun onEmailVerificationPassed(dataPoint: DataPoint) {
        dataPoint.verification?.let { verification ->
            verification.secondaryCredential?.let {
                popFragment()
                primaryVerification = verification
                if (it.status == VerificationStatus.PASSED) {
                    loginUser(verification, it)
                } else {
                    if (it.verificationType == AUTH_TYPE_PHONE) {
                        val fragment = fragmentFactory.inputPhoneFragment(
                                uiTheme = UIConfig.uiTheme,
                                allowedCountries = contextConfiguration.projectConfiguration.allowedCountries,
                                tag = INPUT_PHONE_TAG
                        )
                        fragment.delegate = this
                        setStartElement(fragment as FlowPresentable)
                    } else {
                        val fragment = fragmentFactory.birthdateVerificationFragment(
                                UIConfig.uiTheme,
                                dataPoint,
                                BIRTHDATE_VERIFICATION_TAG)
                        fragment.delegate = this
                        push(fragment as BaseFragment)
                    }
                }
            } ?: createUser(dataPoint)
        }
    }

    //
    // Verify Birthdate
    //
    override fun onBackFromBirthdateVerification() = popFragment()

    override fun onBirthdateVerificationPassed(primaryCredentialVerification: Verification, birthdateVerification: Verification) {
        primaryVerification?.let { primaryVerification ->
            loginUser(primaryVerification, birthdateVerification)
        }
    }

    //
    // Create user and login
    //
    private fun createUser(dataPoint: DataPoint) {
        showLoading()
        AptoPlatform.createUser(DataPointList().add(dataPoint)) { result ->
            hideLoading()
            result.either(::handleFailure) { user ->
                analyticsManager.createUser(user.userId)
                onFinish(user.token)
            }
        }
    }

    private fun loginUser(primaryVerification: Verification, secondaryVerification: Verification) {
        showLoading()
        AptoPlatform.loginUserWith(listOf(primaryVerification, secondaryVerification)) { result ->
            hideLoading()
            result.either(::handleFailure) { user ->
                analyticsManager.loginUser(user.userId)
                onFinish(user.token)
            }
        }
    }
}
