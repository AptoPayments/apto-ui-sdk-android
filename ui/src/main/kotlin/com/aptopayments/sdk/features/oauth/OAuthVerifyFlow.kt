package com.aptopayments.sdk.features.oauth

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.oauth.OAuthAttempt
import com.aptopayments.mobile.data.user.DataPointList
import com.aptopayments.mobile.data.workflowaction.AllowedBalanceType
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.oauth.verify.OAuthVerifyContract
import org.koin.core.inject

private const val OAUTH_VERIFY_TAG = "OAuthVerifyFragment"

internal class OAuthVerifyFlow(
    val allowedBalanceType: AllowedBalanceType,
    val oauthAttempt: OAuthAttempt,
    val onBack: (Unit) -> Unit,
    val onFinish: (oauthAttempt: OAuthAttempt) -> Unit,
    val onError: (Failure.ServerError) -> Unit
) : Flow(), OAuthVerifyContract.Delegate {

    val analyticsManager: AnalyticsServiceContract by inject()
    private val oAuthVerifyFragment: OAuthVerifyContract.View?
        get() = fragmentWithTag(OAUTH_VERIFY_TAG) as? OAuthVerifyContract.View

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        oauthAttempt.userData?.let {
            val fragment = fragmentFactory.oauthVerifyFragment(
                datapoints = it,
                allowedBalanceType = allowedBalanceType,
                tokenId = oauthAttempt.tokenId,
                tag = OAUTH_VERIFY_TAG
            )
            fragment.delegate = this
            setStartElement(element = fragment as FlowPresentable)
            onInitComplete(Either.Right(Unit))
        } ?: onInitComplete(Either.Left(OAuthVerifyFlowInitFailure()))
    }

    override fun restoreState() {
        oAuthVerifyFragment?.delegate = this
    }

    //
    // OAuthVerifyContract handling
    //
    override fun onAcceptPii(updatedDataPoints: DataPointList) {
        analyticsManager.track(Event.SelectBalanceStoreOauthConfirmTap)
        oauthAttempt.userData = updatedDataPoints
        onFinish(oauthAttempt)
    }

    override fun onBackFromOAuthVerify() {
        analyticsManager.track(Event.SelectBalanceStoreOauthConfirmConfirmBackTap)
        popFragment()
    }

    override fun onRevokedTokenError(failure: Failure.ServerError) {
        onError(failure)
    }

    fun showUpdatedUserData(dataPointList: DataPointList) {
        oAuthVerifyFragment?.updateDataPoints(dataPointList)
    }
}

internal class OAuthVerifyFlowInitFailure : Failure.FeatureFailure()
