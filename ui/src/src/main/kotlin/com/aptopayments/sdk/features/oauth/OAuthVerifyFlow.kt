package com.aptopayments.sdk.features.oauth

import androidx.annotation.VisibleForTesting
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.oauth.OAuthAttempt
import com.aptopayments.core.data.user.DataPointList
import com.aptopayments.core.data.workflowaction.AllowedBalanceType
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.oauth.verify.OAuthVerifyContract
import org.koin.core.inject
import java.lang.reflect.Modifier

private const val OAUTH_VERIFY_TAG = "OAuthVerifyFragment"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
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
                    uiTheme = UIConfig.uiTheme,
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
