package com.aptopayments.sdk.features.auth

import com.aptopayments.mobile.data.config.ContextConfiguration
import com.aptopayments.mobile.data.user.DataPoint
import com.aptopayments.mobile.data.user.DataPointList
import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.auth.birthdateverification.BirthdateVerificationContract
import com.aptopayments.sdk.features.auth.inputemail.InputEmailContract
import com.aptopayments.sdk.features.auth.inputphone.InputPhoneContract
import com.aptopayments.sdk.features.auth.verification.EmailVerificationContract
import com.aptopayments.sdk.features.auth.verification.PhoneVerificationContract
import com.aptopayments.sdk.repository.InitializationDataRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

private const val INPUT_PHONE_TAG = "InputPhoneFragment"
private const val PHONE_VERIFICATION_TAG = "PhoneVerificationFragment"
private const val EMAIL_VERIFICATION_TAG = "EmailVerificationFragment"
private const val BIRTHDATE_VERIFICATION_TAG = "BirthdateVerificationFragment"
private const val INPUT_EMAIL_TAG = "InputEmailFragment"

internal class AuthFlow(
    private val contextConfiguration: ContextConfiguration,
    private val onBack: () -> Unit,
    private val onFinish: (userToken: String) -> Unit
) : Flow(),
    InputPhoneContract.Delegate,
    InputEmailContract.Delegate,
    PhoneVerificationContract.Delegate,
    EmailVerificationContract.Delegate,
    BirthdateVerificationContract.Delegate,
    KoinComponent {

    private val analyticsManager: AnalyticsServiceContract by inject()
    private val initializationData: InitializationDataRepository by inject()
    private var primaryVerification: Verification? = null

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = when (contextConfiguration.projectConfiguration.primaryAuthCredential) {
            DataPoint.Type.EMAIL -> createInputEmailFragment() as FlowPresentable
            else -> createPhoneInputFragment() as FlowPresentable
        }
        setStartElement(fragment)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(INPUT_PHONE_TAG) as? InputPhoneContract.View)?.let { it.delegate = this }
        (fragmentWithTag(INPUT_EMAIL_TAG) as? InputEmailContract.View)?.let { it.delegate = this }
        (fragmentWithTag(PHONE_VERIFICATION_TAG) as? PhoneVerificationContract.View)?.let { it.delegate = this }
        (fragmentWithTag(BIRTHDATE_VERIFICATION_TAG) as? BirthdateVerificationContract.View)?.let { it.delegate = this }
        (fragmentWithTag(EMAIL_VERIFICATION_TAG) as? EmailVerificationContract.View)?.let { it.delegate = this }
    }

    override fun onBackFromInputPhone() = onBack()

    override fun onPhoneVerificationStarted(verification: Verification) {
        val fragment = fragmentFactory.phoneVerificationFragment(verification, PHONE_VERIFICATION_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun onBackFromInputEmail() = onBack.invoke()

    override fun onEmailVerificationStarted(verification: Verification) {
        val fragment = fragmentFactory.emailVerificationFragment(verification, EMAIL_VERIFICATION_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun onBackFromPhoneVerification() = popFragment()

    override fun onPhoneVerificationPassed(dataPoint: DataPoint) = onAuthStepPassed(dataPoint)

    override fun onBackFromEmailVerification() = popFragment()

    override fun onEmailVerificationPassed(dataPoint: DataPoint) = onAuthStepPassed(dataPoint)

    override fun onBackFromBirthdateVerification() = popFragment()

    override fun onBirthdateVerificationPassed(dataPoint: DataPoint) = onAuthStepPassed(dataPoint)

    private fun onAuthStepPassed(dataPoint: DataPoint) {
        if (dataPoint.getType() == contextConfiguration.projectConfiguration.secondaryAuthCredential) {
            loginUser(primaryVerification!!, dataPoint.verification!!)
        } else {
            primaryVerification = dataPoint.verification
            onNextTapped(dataPoint)
        }
    }

    private fun onNextTapped(dataPoint: DataPoint) {
        val secondary = dataPoint.verification?.secondaryCredential
        if (secondary == null) {
            createUser(dataPoint)
        } else {
            launchSecondFactorAuthentication(secondary.verificationType)
        }
    }

    private fun launchSecondFactorAuthentication(type: String) {
        val fragment = when (type.toUpperCase()) {
            DataPoint.Type.EMAIL.toString() -> createInputEmailFragment() as BaseFragment
            DataPoint.Type.BIRTHDATE.toString() -> createBirthdateVerificationFragment(primaryVerification!!) as BaseFragment
            DataPoint.Type.PHONE.toString() -> createPhoneInputFragment() as BaseFragment
            else -> createInputEmailFragment() as BaseFragment
        }
        push(fragment)
    }

    //
    // Create user and login
    //
    private fun createUser(dataPoint: DataPoint) {
        showLoading()
        AptoPlatform.createUser(
            userData = DataPointList().add(dataPoint),
            custodianUid = initializationData.data?.custodianUid,
            metadata = initializationData.data?.userMetadata
        ) { result ->
            hideLoading()
            result.either(::handleFailure) { user ->
                initializationData.data?.userMetadata = null
                initializationData.data?.custodianUid = null
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

    private fun createInputEmailFragment(): InputEmailContract.View {
        val fragment = fragmentFactory.inputEmailFragment(INPUT_EMAIL_TAG)
        fragment.delegate = this
        return fragment
    }

    private fun createPhoneInputFragment(): InputPhoneContract.View {
        val fragment = fragmentFactory.inputPhoneFragment(
            contextConfiguration.projectConfiguration.allowedCountries,
            INPUT_PHONE_TAG
        )
        fragment.delegate = this
        return fragment
    }

    private fun createBirthdateVerificationFragment(verification: Verification): BirthdateVerificationContract.View {
        val fragment =
            fragmentFactory.birthdateVerificationFragment(verification, BIRTHDATE_VERIFICATION_TAG)
        fragment.delegate = this
        return fragment
    }
}
